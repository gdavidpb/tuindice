package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.CountingSessionInvalidationRepository
import com.gdavidpb.tuindice.testing.ProductionLikeApplicationRepository
import com.gdavidpb.tuindice.testing.RealSessionStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// The invalidation belongs to the app, not to whoever asked for it. The wipe empties the very stores the
// session is read from, so the moment its first step runs the session is gone: whatever happens to the
// caller after that, the end of the session must come out whole. It is checked here with the real
// session store over the real stores, and a wipe that does what the platforms' wipes do, cancelling the
// caller at each step of the invalidation. The invariant, whenever the job ends: the session is gone
// only together with its notice persisted, its signal emitted once and its counter advanced (a second
// caller that was waiting for the lock is served by that wipe instead of wiping again).
@OptIn(ExperimentalCoroutinesApi::class)
class SessionInvalidationOwnershipTest {
	@Test
	fun aCallerCancelledBeforeTheInvalidationStarts_stillEndsTheSessionWhole() = runTest {
		val rig = Rig(this)
		rig.start()

		// Cancelled while it waits for the job, which has not run a single step yet.
		rig.callerOutside.cancel()
		advanceUntilIdle()

		rig.assertEndedWhole()
	}

	@Test
	fun aCallerCancelledDuringTheWipeOfTheData_inAStepThatChecksCancellation_stillEndsTheSessionWhole() = runTest {
		val rig = Rig(this)
		rig.database = {
			rig.callerOutside.cancel()
			yield()
			delay(1)
		}
		rig.start()

		advanceUntilIdle()

		rig.assertEndedWhole()
	}

	@Test
	fun aCallerCancelledDuringTheWipeOfTheData_inAStepThatDoesNotCheckIt_stillEndsTheSessionWhole() = runTest {
		val rig = Rig(this)
		rig.database = { rig.callerOutside.cancel() }
		rig.start()

		advanceUntilIdle()

		rig.assertEndedWhole()
	}

	@Test
	fun aCallerCancelledWhileTheSessionStoreWaitsForItsLock_stillEndsTheSessionWhole() = runTest {
		val rig = Rig(this)
		var gate: CompletableDeferred<Unit>? = null
		rig.database = {
			// Somebody holds the lock of the session store, reading a token the keystore is slow to give.
			gate = rig.store.active.gateReads()
			rig.testScope.launch(start = CoroutineStart.UNDISPATCHED) { rig.store.repository.getActiveSessionSnapshot() }
			rig.callerOutside.cancel()
		}
		rig.start()
		runCurrent()

		assertNotNull(gate, "the lock was taken")
		assertEquals(0, rig.invalidations.raised, "the wipe waits for the lock: nothing is announced yet")

		gate?.complete(Unit)
		advanceUntilIdle()

		rig.assertEndedWhole()
	}

	@Test
	fun aCallerCancelledBetweenTheNoticeAndTheCounter_stillEndsTheSessionWhole() = runTest {
		val rig = Rig(this)
		// The report is the step between the notice and the counter.
		rig.onReport = { rig.callerOutside.cancel() }
		rig.start()

		advanceUntilIdle()

		rig.assertEndedWhole()
	}

	@Test
	fun aCallerThatIsNotCancelled_getsItsNormalReturnWhenEverythingIsDone() = runTest {
		val rig = Rig(this)
		rig.start()

		advanceUntilIdle()

		rig.assertEndedWhole()
		assertFalse(rig.callerOutside.isCancelled)
		assertTrue(rig.callerOutside.isCompleted)
	}

	// One inside the scope and one outside it at once: one wipe, one notice, one signal. The one inside
	// unwinds as a cancellation, the one outside returns normally.
	@Test
	fun twoCallers_oneInsideTheScopeAndOneOutside_wipeOnceAndAnnounceOnce() = runTest {
		val rig = Rig(this)
		var insideRanAfter = false
		val inside = rig.sessionScope.launch(start = CoroutineStart.UNDISPATCHED) {
			rig.recovery.invalidateSession(sessionId = "session-old")
			insideRanAfter = true
		}
		rig.start()

		advanceUntilIdle()

		rig.assertEndedWhole()
		assertTrue(inside.isCancelled, "the caller inside the scope unwinds as a cancellation")
		assertFalse(insideRanAfter, "and nothing it had next runs")
		assertFalse(rig.callerOutside.isCancelled)
		assertTrue(rig.callerOutside.isCompleted)
	}

	@Test
	fun aCallerInsideTheScope_unwindsAsACancellation_andWritesNothingAfterwards() = runTest {
		val rig = Rig(this)
		rig.store.seed()
		var wroteAfterwards = false
		val inside = rig.sessionScope.launch(start = CoroutineStart.UNDISPATCHED) {
			rig.recovery.invalidateSession(sessionId = "session-old")
			wroteAfterwards = true
			rig.store.seed(sessionId = "written-after")
		}

		advanceUntilIdle()

		assertTrue(inside.isCancelled)
		assertFalse(wroteAfterwards)
		assertTrue(rig.store.isGone(), "nothing of its own was written into the emptied stores")
		assertEquals(1, rig.invalidations.raised)
		assertEquals(SessionResetNoticeReason.ServerEndedSession, rig.settings.sessionResetNoticeReason)
		assertTrue(rig.reporting.reports.single().contains("inside the session scope"))
	}

	// The origin of the call is read from the caller, since the job that wipes runs in the app's scope.
	@Test
	fun aCallerOutsideTheScope_isReportedAsOutside() = runTest {
		val rig = Rig(this)
		rig.start()

		advanceUntilIdle()

		assertTrue(rig.reporting.reports.single().contains("outside the session scope"))
	}

	// Cancelled with the rest of the scope's work, whatever it was waiting for: the invalidation is
	// the one thing the scope cannot cancel, because it does not hang from it.
	@Test
	fun theWorkOfTheSessionScope_isCancelledByTheInvalidation_butTheInvalidationIsNot() = runTest {
		val rig = Rig(this)
		var requestCancelled = false
		rig.sessionScope.launch(start = CoroutineStart.UNDISPATCHED) {
			try {
				awaitCancellation()
			} finally {
				requestCancelled = true
			}
		}
		rig.start()

		advanceUntilIdle()

		assertTrue(requestCancelled)
		rig.assertEndedWhole()
	}

	private class Rig(val testScope: TestScope) {
		val settings = FakeSettingsRepository()
		val invalidations = CountingSessionInvalidationRepository()
		val store = RealSessionStore(settings = settings, invalidations = invalidations)
		val reporting = HookedReportingRepository { onReport() }

		// What the test makes the wipe of the database do (it stands for the database).
		var database: suspend () -> Unit = {}
		var onReport: () -> Unit = {}

		private val dispatcher = StandardTestDispatcher(testScope.testScheduler)
		val sessionScope = testSessionCoroutineScope(dispatcher)
		val application = ProductionLikeApplicationRepository(
			settings = settings,
			stores = listOf(store.active, store.legacy),
			memory = listOf(store.memory),
			database = { database() }
		)
		val recovery = SessionRecoveryDataSource(
			sessionRepository = store.repository,
			applicationRepository = application,
			sessionInvalidationRepository = invalidations,
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = object : AttestationRepository {
				override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
			},
			authRepository = OwnershipUnusedAuthRepository,
			credentialsRepository = FakeCredentialsRepository(),
			settingsRepository = settings,
			reportingRepository = reporting,
			sessionCoroutineScope = sessionScope,
			appCoroutineScope = testAppCoroutineScope(dispatcher)
		)

		// The first caller, outside the session scope, and a second one that arrives behind it: it is
		// served by the wipe of the first, and only if that wipe advanced the counter.
		lateinit var callerOutside: Job
		lateinit var secondCaller: Job

		suspend fun start() {
			store.seed()

			callerOutside = testScope.launch(start = CoroutineStart.UNDISPATCHED) {
				recovery.invalidateSession(sessionId = "session-old")
			}
			secondCaller = testScope.launch(start = CoroutineStart.UNDISPATCHED) {
				recovery.invalidateSession(sessionId = "session-old")
			}
		}

		suspend fun assertEndedWhole() {
			assertTrue(store.isGone(), "the session is gone")
			assertEquals(
				SessionResetNoticeReason.ServerEndedSession,
				settings.sessionResetNoticeReason,
				"with its notice persisted and its reason"
			)
			assertEquals(1, invalidations.raised, "its signal emitted exactly once")
			assertEquals(1, application.clearCalls, "and wiped once: the counter advanced, so the second caller was served")
			assertEquals(1, reporting.reports.size, "and reported once")
			assertTrue(secondCaller.isCompleted && !secondCaller.isCancelled)
		}
	}
}

private class HookedReportingRepository(private val onLog: () -> Unit) : ReportingRepository {
	val reports = mutableListOf<String>()

	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) {
		reports += throwable.message.orEmpty()
		onLog()
	}

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private object OwnershipUnusedAuthRepository : AuthRepository {
	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(sessionId: String, refreshToken: String, attestation: Attestation): RefreshTokens =
		error("unused")

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}
