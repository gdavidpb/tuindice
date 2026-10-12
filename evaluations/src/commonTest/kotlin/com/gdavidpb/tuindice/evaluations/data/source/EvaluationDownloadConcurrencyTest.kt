@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeMutationEnvelopeStore
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.evaluations.testing.createEvaluationsMutationEngine
import com.gdavidpb.tuindice.evaluations.utils.CooldownTimes
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Who waits for whom around a download: the lock covers the download and its save, the tab and the
// revalidation after a sync share one, the student's writes and the draining never wait for it, and
// a download that outlives its session leaves no trace.
class EvaluationDownloadConcurrencyTest {
	@Test
	fun aCallerThatDoesNotForce_sharesTheDownloadInProgress_evenWithoutACooldown() = runTest {
		val api = HeldEvaluationsApi()
		val repository = repository(api = api)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		val tab = launch { repository.updateEvaluations() }
		runCurrent()
		assertEquals(1, api.calls, "the tab joins the download in progress")

		api.release()
		runCurrent()

		assertTrue(revalidation.isCompleted && tab.isCompleted)
		assertEquals(1, api.calls)
	}

	@Test
	fun aCallerThatJoins_getsTheSameFailure_withoutASecondRequest() = runTest {
		val failure = IllegalStateException("offline")
		val api = HeldEvaluationsApi()
		val repository = repository(api = api)
		var revalidationFailure: Throwable? = null
		var tabFailure: Throwable? = null

		launch { runCatching { repository.revalidateEvaluations() }.onFailure { revalidationFailure = it } }
		runCurrent()
		launch { runCatching { repository.updateEvaluations() }.onFailure { tabFailure = it } }
		runCurrent()

		api.release(failure)
		runCurrent()

		assertSame(failure, revalidationFailure)
		assertSame(failure, tabFailure)
		assertEquals(1, api.calls, "the failure is shared, not retried")
	}

	@Test
	fun aForcedCaller_downloadsAgainAfterTheOneInProgress() = runTest {
		val api = HeldEvaluationsApi()
		val repository = repository(api = api)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		val forced = launch { repository.updateEvaluations(forceRemote = true) }
		runCurrent()
		assertEquals(1, api.calls, "the forced caller queues behind the download")

		api.release()
		runCurrent()

		assertTrue(revalidation.isCompleted && forced.isCompleted)
		assertEquals(2, api.calls, "and then makes its own")
	}

	@Test
	fun aCallerThatJoined_startsItsOwn_whenTheOneItJoinedWasCancelled() = runTest {
		val api = HeldEvaluationsApi()
		val repository = repository(api = api)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		val tab = launch { repository.updateEvaluations() }
		runCurrent()

		revalidation.cancel()
		runCurrent()
		api.release()
		runCurrent()

		assertTrue(tab.isCompleted && !tab.isCancelled, "a cancellation is not the tab's failure")
		assertEquals(2, api.calls)
	}

	@Test
	fun theDrainDoesNotHoldTheDownloadLock() = runTest {
		val api = HeldEvaluationsApi(holdAdds = true)
		val repository = repository(api = api, store = storeWithPendingAdd())

		// The revalidation downloads, saves and then drains: its drain is stuck on the held add.
		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		api.release()
		runCurrent()
		assertTrue(!revalidation.isCompleted, "its drain is waiting for the add")
		assertEquals(1, api.calls)

		val forced = launch { repository.updateEvaluations(forceRemote = true) }
		runCurrent()

		assertEquals(2, api.calls, "the next download does not wait for another caller's drain")
		forced.cancel()
		revalidation.cancel()
	}

	@Test
	fun addEvaluation_completes_whileADownloadIsHeld() = runTest {
		val api = HeldEvaluationsApi()
		val repository = repository(api = api)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		val write = launch { repository.addEvaluation(quizAdd()) }
		runCurrent()

		assertTrue(write.isCompleted, "a student's write never waits for a download")
		assertTrue(!revalidation.isCompleted)
		revalidation.cancel()
	}

	@Test
	fun aPendingChangeAnsweredWithConflictPreconditionOrNotFound_doesNotDeadlockEitherCaller() = runTest {
		listOf(HttpStatusCode.Conflict, HttpStatusCode.PreconditionFailed, HttpStatusCode.NotFound)
			.forEach { status ->
				val api = FakeEvaluationsApiDataSource(
					addThrowable = clientRequestException(status, message = "status ${status.value}")
				)
				val repository = repository(api = api, store = storeWithPendingAdd())

				val revalidation = launch { repository.revalidateEvaluations() }
				val tab = launch { repository.updateEvaluations(forceRemote = true) }
				runCurrent()

				assertTrue(revalidation.isCompleted, "revalidation finishes after a $status")
				assertTrue(tab.isCompleted, "the tab finishes after a $status")
			}
	}

	@Test
	fun aCancelledDownload_doesNotHoldTheNextRevalidationOff() = runTest {
		val api = HeldEvaluationsApi()
		val repository = repository(api = api)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		revalidation.cancel()
		runCurrent()
		val next = launch { repository.revalidateEvaluations() }
		runCurrent()

		assertEquals(2, api.calls, "a cancellation is not a failed download")
		api.release()
		runCurrent()
		assertTrue(next.isCompleted)
	}

	@Test
	fun theHoldAfterAFailure_expiresWithTheRetentionWindow() = runTest {
		val clock = MutableClock()
		val api = FakeEvaluationsApiDataSource(getEvaluationsThrowable = IllegalStateException("offline"))
		val repository = repository(api = api, clock = clock)

		assertFailsWith<IllegalStateException> { repository.revalidateEvaluations() }
		clock.advance(CooldownTimes.COOLDOWN_RETRY_EVALUATIONS - 1)
		repository.revalidateEvaluations()
		assertEquals(1, api.getEvaluationsCalls, "still held one millisecond before the window ends")

		clock.advance(1)
		assertFailsWith<IllegalStateException> { repository.revalidateEvaluations() }
		assertEquals(2, api.getEvaluationsCalls, "tries again once the window is over")
	}

	@Test
	fun aFailureThatEndedTheSession_doesNotHoldTheNextAccountOff() = runTest {
		lateinit var repository: EvaluationDataSource
		// A 401: the session is wiped first, then the request throws.
		val api = HeldEvaluationsApi(onCall = { repository.clearSessionMemory() })
		repository = repository(api = api)

		val first = launch { runCatching { repository.revalidateEvaluations() } }
		runCurrent()
		api.release(IllegalStateException("401"))
		runCurrent()
		assertTrue(first.isCompleted)

		launch { runCatching { repository.revalidateEvaluations() } }
		runCurrent()

		assertEquals(2, api.calls)
	}

	@Test
	fun aDownloadThatLandsAfterTheSessionWasCleared_savesNothingAndMarksNoCooldown() = runTest {
		val api = HeldEvaluationsApi()
		val database = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
		)
		val settings = FakeSettingsDataSource(onCooldown = false)
		val repository = repository(api = api, database = database, settings = settings)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		repository.clearSessionMemory()
		api.release()
		runCurrent()

		assertTrue(revalidation.isCompleted)
		assertTrue(database.savedSnapshots.isEmpty(), "the previous account's snapshot is not stored")
		assertEquals(false, settings.cooldownMarked, "and it marks no cooldown for the next one")
	}

	@Test
	fun aCallerOfTheNextSession_doesNotJoinADownloadOfThePreviousOne() = runTest {
		val api = HeldEvaluationsApi()
		val repository = repository(api = api)

		launch { runCatching { repository.revalidateEvaluations() } }
		runCurrent()
		repository.clearSessionMemory()
		val tab = launch { repository.updateEvaluations() }
		runCurrent()
		api.release()
		runCurrent()

		assertTrue(tab.isCompleted)
		assertEquals(2, api.calls, "it queues for the lock and makes its own request")
	}

	private fun TestScope.repository(
		api: EvaluationsApiDataRepository = FakeEvaluationsApiDataSource(),
		database: FakeDatabaseDataSource = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
		),
		settings: FakeSettingsDataSource = FakeSettingsDataSource(onCooldown = false),
		store: FakeMutationEnvelopeStore<String, EvaluationMutation> = FakeMutationEnvelopeStore(),
		clock: Clock = Clock.System
	) = EvaluationDataSource(
		databaseDataSource = database,
		evaluationsApiDataSource = api,
		settingsDataSource = settings,
		mutationEngine = createEvaluationsMutationEngine(store = store),
		identifierRepository = FakeIdentifierRepository(),
		clock = clock
	)

	private fun quizAdd() = EvaluationAdd(
		reference = "reference-1",
		attemptId = DEFAULT_EVALUATION_SUBJECT.id,
		subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
		termId = DEFAULT_EVALUATION_SUBJECT.termId,
		type = EvaluationType.QUIZ,
		scheduleMode = EvaluationScheduleMode.DATED,
		date = 1_900_000_000_000L,
		grade = null,
		maxGrade = 100.0
	)

	private fun storeWithPendingAdd() = FakeMutationEnvelopeStore(
		initialPendingMutations = listOf(
			MutationEnvelope<String, EvaluationMutation>(
				mutationId = "pending-1",
				scopeKey = EVALUATIONS_MUTATION_SCOPE,
				command = EvaluationMutation.Add(
					referenceId = "reference-pending",
					attemptId = DEFAULT_EVALUATION_SUBJECT.id,
					subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
					termId = DEFAULT_EVALUATION_SUBJECT.termId,
					scheduleMode = EvaluationScheduleMode.DATED,
					grade = null,
					maxGrade = 100.0,
					date = 1_900_000_000_000L,
					type = EvaluationType.QUIZ.ordinal
				),
				precondition = MutationPrecondition.None,
				status = PendingMutationStatus.Pending,
				createdAt = 1L,
				updatedAt = 1L,
				lastError = null
			)
		)
	)
}

// Holds every download until released (once released, later ones go straight through), and can hold
// the sending of an add too.
private class HeldEvaluationsApi(
	private val holdAdds: Boolean = false,
	private val onCall: suspend () -> Unit = {},
	private val delegate: FakeEvaluationsApiDataSource = FakeEvaluationsApiDataSource()
) : EvaluationsApiDataRepository by delegate {
	private val gate = CompletableDeferred<Unit>()
	private var failure: Throwable? = null
	var calls = 0
		private set

	fun release(failure: Throwable? = null) {
		this.failure = failure
		gate.complete(Unit)
	}

	override suspend fun getEvaluations(): RemoteEvaluationsSnapshot {
		calls++
		onCall()
		gate.await()
		failure?.let { throw it }
		return delegate.getEvaluations()
	}

	override suspend fun addEvaluation(
		add: EvaluationMutation.Add,
		mutationId: String
	): EvaluationMutationAck.Add {
		if (holdAdds) CompletableDeferred<Unit>().await()
		return delegate.addEvaluation(add, mutationId)
	}
}

private class MutableClock : Clock {
	private var nowMillis = 1_000_000_000L

	fun advance(millis: Long) {
		nowMillis += millis
	}

	override fun now(): Instant = Instant.fromEpochMilliseconds(nowMillis)
}
