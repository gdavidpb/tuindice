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
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
		// The order of the real wipe: the holders are told when it begins, the download lands while
		// the stores are still being emptied, and they are told again when it ends.
		repository.clearSessionMemory()
		api.release()
		runCurrent()
		repository.clearSessionMemory()

		assertTrue(revalidation.isCompleted)
		assertTrue(database.savedSnapshots.isEmpty(), "the previous account's snapshot is not stored")
		assertEquals(false, settings.cooldownMarked, "and it marks no cooldown for the next one")
	}

	// The download a 409 asks for while a drain runs has the same guard as the others.
	@Test
	fun theSnapshotAConflictDownloads_afterTheSessionWasCleared_isNotStored() = runTest {
		val api = HeldEvaluationsApi(
			delegate = FakeEvaluationsApiDataSource(
				addThrowable = clientRequestException(HttpStatusCode.Conflict)
			)
		)
		val database = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
		)
		val repository = repository(api = api, database = database, store = storeWithPendingAdd())

		val drain = launch { repository.drainPendingMutations() }
		runCurrent()
		assertEquals(1, api.calls, "the conflict asked for the download")
		repository.clearSessionMemory()
		api.release()
		runCurrent()

		assertTrue(drain.isCompleted)
		assertTrue(database.savedSnapshots.isEmpty(), "the previous account's snapshot is not stored")
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

	// The wipe of the request's own rejection (a 401 that ends the session from inside the request) must
	// not be cut by the cancellation the end of the session sends to the request in flight: it would
	// stop half way. The request is the caller here, so it is left running.
	@Test
	fun theEndOfTheSession_doesNotCancelTheRequestThatCausesIt() = runTest {
		lateinit var repository: EvaluationDataSource
		var activeAfterTheWipe: Boolean? = null
		val api = HeldEvaluationsApi(
			onCall = {
				repository.clearSessionMemory()
				activeAfterTheWipe = currentCoroutineContext().isActive
			}
		)
		repository = repository(api = api)

		launch { runCatching { repository.revalidateEvaluations() } }
		runCurrent()

		assertEquals(true, activeAfterTheWipe, "the request that ends the session is not cancelled by its own wipe")
		api.release()
		runCurrent()
	}

	// A caller that queued for the lock before the session ended and gets it afterwards has nobody to
	// answer to: it makes no request for the account that is gone.
	@Test
	fun aCallerThatWaitedForTheLock_throughTheEndOfItsSession_makesNoRequest() = runTest {
		val api = HeldEvaluationsApi()
		val settings = HeldSettings()
		val repository = repository(api = api, settings = settings)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		repository.clearSessionMemory()
		settings.release()
		api.release()
		runCurrent()

		assertTrue(revalidation.isCancelled, "its session ended while it decided")
		assertEquals(0, api.calls, "and the account that is gone gets no request")
	}

	// The session can end after the epoch was checked and before the wipe can find the request, which
	// is created first and only then stored where the wipe looks for it. The request is then cancelled by
	// its owner, before it makes the call. The window holds no suspension, so the test ends the session
	// from the dispatcher, at the very moment the request is handed to it.
	@Test
	fun aRequestMadeAsTheSessionEnds_isCancelledBeforeItMakesTheCall() = runTest {
		lateinit var repository: EvaluationDataSource
		val dispatcher = HookedDispatcher(StandardTestDispatcher(testScheduler))
		val api = HeldEvaluationsApi()
		val database = HookedDecisionDatabase(
			delegate = FakeDatabaseDataSource(
				initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
			),
			afterTheDecisionIsRead = {
				dispatcher.onNextDispatch = { runBlocking { repository.clearSessionMemory() } }
			}
		)
		repository = repository(api = api, database = database)

		val revalidation = launch(dispatcher) { repository.revalidateEvaluations() }
		runCurrent()

		assertTrue(revalidation.isCancelled, "the request of the session that ended is not waited for")
		assertEquals(0, api.calls, "and it never makes the call")
	}

	// A caller of an old session registers its download only if it still belongs to the session: the
	// registration of the new one, made while the old one waited for the lock, must survive its attempt,
	// so that whoever arrives afterwards shares it instead of making a second request.
	@Test
	fun anOwnerOfAnOldSession_doesNotErase_theDownloadRegisteredByTheNewOne() = runTest {
		val api = HeldEvaluationsApi()
		val settings = HeldSettings()
		val repository = repository(api = api, settings = settings)

		// The old owner decides (it holds the lock and waits for the settings) and a caller joins it.
		launch { runCatching { repository.revalidateEvaluations() } }
		runCurrent()
		val joiner = launch { repository.updateEvaluations() }
		runCurrent()
		// The session ends; the first caller of the next one registers its download and queues.
		repository.clearSessionMemory()
		val next = launch { repository.updateEvaluations() }
		runCurrent()

		settings.release()
		api.release()
		runCurrent()

		assertTrue(joiner.isCompleted && next.isCompleted)
		assertEquals(1, api.calls, "the one who started over joined the registration of the new session")
	}

	// The decision to download reads two stores before the request goes out. A caller that arrives
	// meanwhile is not a second opinion: it shares what the one deciding ends up with.
	@Test
	fun aCallerThatArrivesWhileAnotherDecides_inheritsItsFailure_withoutASecondRequest() = runTest {
		val failure = IllegalStateException("offline")
		val api = HeldEvaluationsApi()
		val settings = HeldSettings()
		val repository = repository(api = api, settings = settings)
		var revalidationFailure: Throwable? = null
		var tabFailure: Throwable? = null

		launch { runCatching { repository.revalidateEvaluations() }.onFailure { revalidationFailure = it } }
		runCurrent()
		assertEquals(0, api.calls, "the first caller is still reading the settings")
		launch { runCatching { repository.updateEvaluations() }.onFailure { tabFailure = it } }
		runCurrent()

		settings.release()
		api.release(failure)
		runCurrent()

		assertSame(failure, revalidationFailure)
		assertSame(failure, tabFailure)
		assertEquals(1, api.calls, "the caller that came in during the decision shares the failure")
	}

	@Test
	fun aCallerThatJoinedADecision_makesNoRequest_whenThatDecisionWasNotToDownload() = runTest {
		val api = HeldEvaluationsApi()
		val settings = HeldSettings(onCooldown = true)
		val database = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(hasSynced = true, evaluations = emptyList())
		)
		val repository = repository(api = api, settings = settings, database = database)

		val revalidation = launch { repository.revalidateEvaluations() }
		runCurrent()
		val tab = launch { repository.updateEvaluations() }
		runCurrent()
		settings.release()
		runCurrent()

		assertTrue(revalidation.isCompleted && tab.isCompleted)
		assertEquals(0, api.calls, "an already downloaded list is not downloaded for the one who waited")
	}

	@Test
	fun aDownloadOfTheSessionThatEnded_isCancelled_soTheLockIsFreeAtOnce() = runTest {
		val api = HeldEvaluationsApi(hangFirstCall = true)
		val repository = repository(api = api)

		val owner = launch { runCatching { repository.updateEvaluations() } }
		runCurrent()
		val joiner = launch { repository.updateEvaluations() }
		runCurrent()
		assertEquals(1, api.calls, "the joiner shares the download that hangs")

		repository.clearSessionMemory()
		runCurrent()

		assertTrue(owner.isCompleted, "the download of the previous session stops")
		// Nothing was released: the request that hangs is gone, and the lock with it. The joiner
		// started over with a request of its own, which only waits for its answer.
		assertEquals(2, api.calls, "the joiner makes its own request instead of inheriting the cancellation")
		api.release()
		runCurrent()
		assertTrue(joiner.isCompleted && !joiner.isCancelled, "its cancellation is not the joiner's")

		val next = launch { repository.updateEvaluations() }
		runCurrent()

		assertTrue(next.isCompleted, "the next session does not wait for the 90 seconds of the old request")
		assertEquals(3, api.calls, "the joiner started over and the next session made its own")
	}

	// A save that already began finishes before the session is told it ended: the wipe that
	// follows then removes it, and one that has not begun finds the new epoch and does not save.
	@Test
	fun theClearing_waitsForASaveInProgress_butNotForTheRequest() = runTest {
		val api = HeldEvaluationsApi()
		val database = HeldSaveDatabase(
			FakeDatabaseDataSource(
				initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
			)
		)
		val repository = repository(api = api, database = database)

		val tab = launch { runCatching { repository.updateEvaluations() } }
		runCurrent()
		repository.clearSessionMemory()
		assertEquals(1, api.calls, "clearing never waits for a request in flight")
		tab.cancel()
		runCurrent()

		val second = HeldSaveDatabase(
			FakeDatabaseDataSource(
				initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
			)
		)
		val api2 = HeldEvaluationsApi().also { it.release() }
		val repository2 = repository(api = api2, database = second)

		val download = launch { repository2.updateEvaluations() }
		runCurrent()
		assertTrue(second.saveStarted, "the answer is being saved")

		var cleared = false
		val clearing = launch {
			repository2.clearSessionMemory()
			cleared = true
		}
		runCurrent()
		assertFalse(cleared, "the clearing waits for the local write that already began")

		second.saveGate.complete(Unit)
		runCurrent()

		assertTrue(cleared && clearing.isCompleted)
		assertEquals(1, second.delegate.savedSnapshots.size, "the save ended before the session did")
		assertTrue(download.isCompleted)
	}

	@Test
	fun theClearing_waitsForTheSaveOfAConflictDownload_too() = runTest {
		val api = HeldEvaluationsApi(
			delegate = FakeEvaluationsApiDataSource(
				addThrowable = clientRequestException(HttpStatusCode.Conflict)
			)
		)
		api.release()
		val database = HeldSaveDatabase(
			FakeDatabaseDataSource(
				initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
			)
		)
		val repository = repository(api = api, database = database, store = storeWithPendingAdd())

		val drain = launch { repository.drainPendingMutations() }
		runCurrent()
		assertTrue(database.saveStarted, "the conflict download is being saved")

		var cleared = false
		launch {
			repository.clearSessionMemory()
			cleared = true
		}
		runCurrent()
		assertFalse(cleared, "the clearing waits for that save as well")

		database.saveGate.complete(Unit)
		runCurrent()

		assertTrue(cleared)
		assertTrue(drain.isCompleted)
	}

	private fun TestScope.repository(
		api: EvaluationsApiDataRepository = FakeEvaluationsApiDataSource(),
		database: DatabaseDataRepository = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
		),
		settings: SettingsDataRepository = FakeSettingsDataSource(onCooldown = false),
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
	private val hangFirstCall: Boolean = false,
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
		// A request that never answers (the 90 seconds of the timeout, as far as the test goes).
		if (hangFirstCall && calls == 1) CompletableDeferred<Unit>().await()
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

// Holds the read of the cooldown, the first of the two reads that precede a download.
private class HeldSettings(
	private val onCooldown: Boolean = false
) : SettingsDataRepository {
	private val gate = CompletableDeferred<Unit>()

	fun release() {
		gate.complete(Unit)
	}

	override suspend fun isGetEvaluationsOnCooldown(): Boolean {
		gate.await()
		return onCooldown
	}

	override suspend fun setGetEvaluationsOnCooldown() = Unit
}

// Holds the save of the confirmed snapshot until told, recording that it began.
private class HeldSaveDatabase(
	val delegate: FakeDatabaseDataSource
) : DatabaseDataRepository by delegate {
	val saveGate = CompletableDeferred<Unit>()
	var saveStarted = false
		private set

	override suspend fun saveConfirmedSnapshot(snapshot: LocalEvaluationsSnapshot) {
		saveStarted = true
		saveGate.await()
		delegate.saveConfirmedSnapshot(snapshot)
	}
}

// Runs a hook right before the next block is handed to the dispatcher, on the thread that hands it.
private class HookedDispatcher(private val delegate: CoroutineDispatcher) : CoroutineDispatcher() {
	var onNextDispatch: (() -> Unit)? = null

	override fun dispatch(context: CoroutineContext, block: Runnable) {
		onNextDispatch?.let { hook ->
			onNextDispatch = null
			hook()
		}
		delegate.dispatch(context, block)
	}
}

// Tells when the two reads that precede a download are over.
private class HookedDecisionDatabase(
	private val delegate: FakeDatabaseDataSource,
	private val afterTheDecisionIsRead: () -> Unit
) : DatabaseDataRepository by delegate {
	override fun observeHasSyncedEvaluationsFlow() = delegate.observeHasSyncedEvaluationsFlow().also {
		afterTheDecisionIsRead()
	}
}
