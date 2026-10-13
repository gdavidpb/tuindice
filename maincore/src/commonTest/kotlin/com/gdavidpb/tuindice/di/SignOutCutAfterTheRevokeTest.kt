package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.CountingSessionInvalidationRepository
import com.gdavidpb.tuindice.testing.ForeignCancellationReporting
import com.gdavidpb.tuindice.testing.ProductionLikeApplicationRepository
import com.gdavidpb.tuindice.testing.RealSessionStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// A manual sign-out is asked for by the dialog's view model, which can be cleared at any moment, but its
// end belongs to the app: once the server revoked the session, cutting the caller does not leave the
// session stored nor the mark of the intentional sign-out behind, because that mark would silence the
// next invalidation and leave the user inside the app with no session and nobody to tell them.
@OptIn(ExperimentalCoroutinesApi::class)
class SignOutCutAfterTheRevokeTest {
	@Test
	fun aSignOutCutWhileItsLocalWipeRuns_endsTheSessionAndWithdrawsItsMark() = runTest {
		val rig = SignOutRig(this)
		rig.holdTheWipe()

		val dialog = launch { rig.signOut.execute(Unit).collect() }
		runCurrent()
		assertTrue(rig.wipeStarted.isCompleted, "the server already revoked the session and the wipe is running")
		dialog.cancel()
		rig.releaseTheWipe()
		advanceUntilIdle()

		assertTrue(rig.store.isGone(), "the cut caller did not leave the revoked session stored")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark of the sign-out is withdrawn")

		// Back in the app, an unrelated session ends: the user is told.
		rig.store.seed(sessionId = "session-new")
		val request = launch { rig.recovery.invalidateSession(sessionId = "session-new") }
		advanceUntilIdle()

		assertTrue(request.isCompleted)
		assertEquals(2, rig.invalidations.raised, "the invalidation after the sign-out is not silenced")
		assertNotNull(rig.settings.sessionResetNotice)
	}

	@Test
	fun aSignOutCutAtEveryLocalStep_stillEndsTheSessionAndWithdrawsItsMark() = runTest {
		for (cutAt in LocalStep.entries) {
			val rig = SignOutRig(this)
			lateinit var dialog: Job
			val cut: suspend () -> Unit = {
				dialog.cancel()
				yield()
			}

			when (cutAt) {
				// The very work the sign-out waits for cuts the caller as it is cancelled.
				LocalStep.CancelActiveWork -> rig.sessionScope.launch {
					try {
						awaitCancellation()
					} finally {
						dialog.cancel()
					}
				}

				LocalStep.ClearData -> rig.onWipe = cut
				LocalStep.ClearSession -> rig.onSessionClear = cut
				LocalStep.ResetSyncStatus -> rig.onSyncStatusReset = cut
			}
			runCurrent()

			dialog = launch { rig.signOut.execute(Unit).collect() }
			advanceUntilIdle()

			assertTrue(dialog.isCancelled, "the caller was cut at $cutAt")
			assertTrue(rig.store.isGone(), "the session is absent after a cut at $cutAt")
			assertTrue(
				rig.invalidations.notifySessionInvalidated(sessionId = null),
				"and the mark is withdrawn after a cut at $cutAt"
			)
		}
	}

	@Test
	fun aSignOutThatRunsToItsEnd_showsNoSessionEndedNotice() = runTest {
		val rig = SignOutRig(this)

		launch { rig.signOut.execute(Unit).collect() }
		advanceUntilIdle()

		assertTrue(rig.store.isGone())
		assertNull(rig.settings.sessionResetNotice, "nobody is owed an explanation of a sign-out")
		assertEquals(0, rig.invalidations.raised)
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "the mark is withdrawn")
	}

	// The sign-out is done once the server revoked the session: a step of the local end that fails is
	// reported, not handed to a caller that can do nothing about it, and the rest of the steps still run.
	// A cancellation that is not the job's is one more failure of the step, not the end of the sign-out.
	@Test
	fun aLocalStepThatFailsAfterTheRevoke_stillEndsTheSignOutAndReportsTheFailure() = runTest {
		val failures = listOf<() -> Throwable>(
			{ IllegalStateException("step failure") },
			{ CancellationException("step failure") }
		)

		for (step in listOf(LocalStep.ClearData, LocalStep.ClearSession, LocalStep.ResetSyncStatus)) {
			for (failure in failures) {
				val label = "$step failing with ${failure()::class.simpleName}"
				val rig = SignOutRig(this)

				when (step) {
					LocalStep.ClearData -> rig.onWipe = { throw failure() }
					// After the stores were emptied: the step failed, the session is gone all the same.
					LocalStep.ClearSession -> rig.onSessionCleared = { throw failure() }
					LocalStep.ResetSyncStatus -> rig.onSyncStatusReset = { throw failure() }
					LocalStep.CancelActiveWork -> Unit
				}

				val states = rig.signOut.execute(Unit).toList()
				advanceUntilIdle()

				assertTrue(states.last() is UseCaseState.Data, "$label: the caller is told the sign-out is done")
				assertTrue(rig.store.isGone(), "$label: the session is absent")
				assertEquals(0, rig.invalidations.raised, "$label: nobody is told the session was closed")
				assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "$label: the mark is withdrawn")
				assertTrue(rig.reporting.loggedExceptions.any { it.message == "step failure" }, "$label: the failure is reported")
			}
		}
	}

	// The report of a failed step does not keep the mark from being withdrawn: a reporter whose call throws a
	// cancellation that is not the job's must not leave the mark behind to silence the next invalidation.
	@Test
	fun aReportThatThrowsACancellation_doesNotKeepTheMarkFromBeingWithdrawn() = runTest {
		val rig = SignOutRig(this, endReporting = ForeignCancellationReporting())
		rig.onWipe = { error("disk failure") }

		val states = rig.signOut.execute(Unit).toList()
		advanceUntilIdle()

		assertTrue(states.last() is UseCaseState.Data, "the caller is told the sign-out is done")
		assertTrue(rig.store.isGone(), "the session is absent")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark is withdrawn")
	}

	// The mark of the sign-out is what silences whatever ends "the same session" while the local end runs.
	@Test
	fun theMarkIsStillThereWhileTheLocalEndRuns() = runTest {
		val rig = SignOutRig(this)
		var suppressedDuringTheWipe: Boolean? = null
		rig.onWipe = {
			suppressedDuringTheWipe = !rig.invalidations.notifySessionInvalidated(sessionId = "session-old")
		}

		launch { rig.signOut.execute(Unit).collect() }
		advanceUntilIdle()

		assertEquals(true, suppressedDuringTheWipe, "the mark was still set while the wipe ran")
		assertEquals(0, rig.invalidations.raised)
	}

	// A request of the signed-out session was already inside its refresh; the server rejects it after the
	// sign-out ended.
	@Test
	fun aRefreshRejectedAfterTheSignOutEnded_isNotAnInvalidation() = runTest {
		val rig = SignOutRig(this)
		val refreshGate = CompletableDeferred<Unit>()
		rig.onRefresh = {
			refreshGate.await()
			throw clientRequestException(HttpStatusCode.Unauthorized)
		}

		val straggler = launch { rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		runCurrent()
		assertEquals(1, rig.refreshCalls, "the refresh of the request is in flight")

		launch { rig.signOut.execute(Unit).toList() }
		advanceUntilIdle()
		assertTrue(rig.store.isGone(), "the sign-out ended")

		refreshGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(straggler.isCompleted)
		assertEquals(1, rig.application.clearCalls, "the session is wiped once")
		assertEquals(0, rig.invalidations.raised, "no signal after a manual sign-out")
		assertNull(rig.settings.sessionResetNotice, "no notice after a manual sign-out")
	}

	// The same rejection lands while the local end is still running (the secure store still holds the
	// session): the end that is in the middle of it serves the invalidation.
	@Test
	fun aRefreshRejectedWhileTheLocalEndRuns_isNotAnInvalidation() = runTest {
		val rig = SignOutRig(this)
		val refreshGate = CompletableDeferred<Unit>()
		rig.onRefresh = {
			refreshGate.await()
			throw clientRequestException(HttpStatusCode.Unauthorized)
		}
		rig.holdTheWipe()

		val straggler = launch { rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		runCurrent()

		launch { rig.signOut.execute(Unit).toList() }
		runCurrent()
		assertTrue(rig.wipeStarted.isCompleted, "the sign-out is wiping the database")

		refreshGate.complete(Unit)
		runCurrent()
		rig.releaseTheWipe()
		advanceUntilIdle()

		assertTrue(straggler.isCompleted)
		assertTrue(rig.store.isGone())
		assertEquals(1, rig.application.clearCalls, "the session is wiped once")
		assertEquals(0, rig.invalidations.raised, "no signal after a manual sign-out")
		assertNull(rig.settings.sessionResetNotice, "no notice after a manual sign-out")
	}

	// The rejection lands while the revoke is still in flight (the mark is set, the local end has not
	// started): the invalidation of the request goes first, silenced by the mark, and the local end of the
	// sign-out finds the session already gone, so the device is wiped once and the user is told by neither.
	@Test
	fun aRefreshRejectedWhileTheRevokeIsInFlight_isNotAnInvalidation() = runTest {
		val rig = SignOutRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		rig.onRevoke = { revokeGate.await() }
		rig.onRefresh = { throw clientRequestException(HttpStatusCode.Unauthorized) }

		launch { rig.signOut.execute(Unit).toList() }
		runCurrent()

		val straggler = launch { rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		advanceUntilIdle()
		revokeGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(straggler.isCompleted)
		assertTrue(rig.store.isGone())
		assertEquals(1, rig.application.clearCalls, "the session is wiped once, by the invalidation that went first")
		assertEquals(0, rig.invalidations.raised, "no signal after a manual sign-out")
		assertNull(rig.settings.sessionResetNotice, "no notice after a manual sign-out")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark is not left behind")
	}

	// Whatever session is stored when the local end runs is not the signed-out one, whether the user signed
	// in again or a recovery by credentials of the same account stored one: the device must be left without it.
	@Test
	fun aStoredSessionThatIsAlreadyAnotherOne_isEndedByTheLocalEndOfTheSignOut() = runTest {
		val rig = SignOutRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		rig.onRevoke = { revokeGate.await() }
		rig.credentials.password = "secret"

		launch { rig.signOut.execute(Unit).toList() }
		runCurrent()

		rig.store.repository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = "session-new",
				accessToken = "access-new",
				refreshToken = "refresh-new",
				usbId = "12-34567"
			)
		)
		revokeGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(rig.store.isGone(), "the device holds no session")
		assertNull(rig.credentials.password, "and not the password it would open one with")
		assertEquals(1, rig.application.clearCalls, "the device was wiped")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark of the sign-out is withdrawn")
	}

	// The identity is read under the lock: an invalidation that was in the middle of its wipe when the revoke
	// returned ends the session first, and the local end, which waited for it, finds nothing left to wipe.
	@Test
	fun aSessionEndedByAnInvalidationWhileTheLocalEndWaitsForTheLock_isNotWipedAgain() = runTest {
		val rig = SignOutRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		rig.onRevoke = { revokeGate.await() }
		rig.holdTheWipe()

		launch { rig.signOut.execute(Unit).toList() }
		runCurrent()
		val invalidation = launch { rig.recovery.invalidateSession(sessionId = "session-old") }
		runCurrent()
		assertTrue(rig.wipeStarted.isCompleted, "the invalidation is in the middle of its wipe")

		revokeGate.complete(Unit)
		runCurrent()
		rig.releaseTheWipe()
		advanceUntilIdle()

		assertTrue(invalidation.isCompleted)
		assertTrue(rig.store.isGone())
		assertEquals(1, rig.application.clearCalls, "the session is wiped once")
		assertEquals(0, rig.invalidations.raised, "no signal after a manual sign-out")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark is withdrawn")
	}

	// A local end that finds the session already gone wipes nothing, so it does not count as a wipe that
	// serves the invalidations waiting behind it: one of them that has a session to end still does.
	@Test
	fun aLocalEndThatSkipsItsSteps_doesNotServeTheInvalidationWaitingBehindIt() = runTest {
		val rig = SignOutRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		val readGate = CompletableDeferred<Unit>()
		rig.onRevoke = { revokeGate.await() }

		launch { rig.signOut.execute(Unit).toList() }
		runCurrent()
		// An invalidation silenced by the mark ended the session while the revoke was in flight.
		rig.store.repository.clear()
		rig.onSnapshotRead = { readGate.await() }
		revokeGate.complete(Unit)
		runCurrent()

		val waiting = launch { rig.recovery.invalidateSession(sessionId = null) }
		runCurrent()
		rig.onSnapshotRead = {}
		readGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(waiting.isCompleted)
		assertEquals(1, rig.application.clearCalls, "the invalidation wiped: the skipped end did not serve it")
		assertEquals(1, rig.invalidations.raised, "and told the user, the mark of the sign-out being gone")
	}

	// The rejection of a request of the signed-out session is released at every point of the sign-out, from
	// a request outside the session scope and from one inside it.
	@Test
	fun aRejectionReleasedAtEveryPointOfTheSignOut_wipesOnceAndTellsNobody() = runTest {
		for (insideTheScope in listOf(false, true)) for (releaseAt in ReleaseAt.entries) {
			val label = "releaseAt=$releaseAt insideTheScope=$insideTheScope"
			val rig = SignOutRig(this)
			val refreshGate = CompletableDeferred<Unit>()
			rig.onRefresh = {
				refreshGate.await()
				throw clientRequestException(HttpStatusCode.Unauthorized)
			}
			val release: suspend () -> Unit = {
				refreshGate.complete(Unit)
				repeat(20) { yield() }
			}
			val scope = if (insideTheScope) rig.sessionScope else this
			val straggler = scope.launch { runCatching { rig.recovery.recoverUnauthorizedSession("access-old", null, null) } }
			runCurrent()
			assertEquals(1, rig.refreshCalls, "$label: refresh in flight")

			when (releaseAt) {
				ReleaseAt.RevokeInFlight -> rig.onRevoke = release
				ReleaseAt.CancelActiveWork -> rig.sessionScope.launch {
					try {
						awaitCancellation()
					} finally {
						refreshGate.complete(Unit)
					}
				}
				ReleaseAt.ClearData -> rig.onWipe = release
				ReleaseAt.ClearSession -> rig.onSessionClear = release
				ReleaseAt.ResetSyncStatus -> rig.onSyncStatusReset = release
				ReleaseAt.AfterTheEnd -> Unit
			}
			runCurrent()

			var states: List<Any>? = null
			launch { states = rig.signOut.execute(Unit).toList() }
			advanceUntilIdle()
			if (releaseAt == ReleaseAt.AfterTheEnd) {
				refreshGate.complete(Unit)
				advanceUntilIdle()
			}

			assertTrue(straggler.isCompleted, "$label: the request ended")
			assertTrue(states?.lastOrNull() is UseCaseState.Data<*>, "$label: the use case says the sign-out is done")
			assertTrue(rig.store.isGone(), "$label: session gone")
			assertEquals(1, rig.application.clearCalls, "$label: one wipe")
			assertEquals(0, rig.invalidations.raised, "$label: no signal")
			assertNull(rig.settings.sessionResetNotice, "$label: no notice")
			assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "$label: mark withdrawn")
		}
	}

	// A new sign-in right after the sign-out; the rejection of a request of the previous session lands then.
	@Test
	fun aNewSignInRightAfterTheSignOut_isNotTouchedByAStragglerOfThePreviousSession() = runTest {
		val rig = SignOutRig(this)
		val refreshGate = CompletableDeferred<Unit>()
		rig.onRefresh = {
			refreshGate.await()
			throw clientRequestException(HttpStatusCode.Unauthorized)
		}
		val straggler = launch { runCatching { rig.recovery.recoverUnauthorizedSession("access-old", null, null) } }
		runCurrent()

		launch { rig.signOut.execute(Unit).toList() }
		advanceUntilIdle()
		assertTrue(rig.store.isGone())

		val newSession = SessionSnapshot("session-new", "access-new", "refresh-new", "12-34567")
		rig.store.repository.setSessionSnapshot(newSession)
		refreshGate.complete(Unit)
		advanceUntilIdle()
		// and a direct invalidation that still names the previous session
		launch { rig.recovery.invalidateSession(sessionId = "session-old") }
		advanceUntilIdle()

		assertTrue(straggler.isCompleted)
		assertEquals(newSession, rig.store.repository.getActiveSessionSnapshot(), "the new session is intact")
		assertEquals(1, rig.application.clearCalls, "one wipe: the sign-out's")
		assertEquals(0, rig.invalidations.raised)
		assertNull(rig.settings.sessionResetNotice)

		// and the new session can still be ended and told
		launch { rig.recovery.invalidateSession(sessionId = "session-new") }
		advanceUntilIdle()
		assertEquals(2, rig.application.clearCalls)
		assertEquals(1, rig.invalidations.raised, "the invalidation of the new session is not silenced")
	}

	// A stored session that cannot be read is not proof that it is gone: the steps run.
	@Test
	fun aStoredSessionThatCannotBeRead_doesNotKeepTheLocalEndFromRunning() = runTest {
		val rig = SignOutRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		rig.onRevoke = { revokeGate.await() }

		launch { rig.signOut.execute(Unit).toList() }
		runCurrent()

		rig.failSnapshotReads = true
		revokeGate.complete(Unit)
		advanceUntilIdle()

		assertEquals(1, rig.application.clearCalls, "the local end ran")
		rig.failSnapshotReads = false
		assertTrue(rig.store.isGone(), "the session is gone")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark is withdrawn")
	}

	// An invalidation that has no session to compare (it cannot be skipped by identity) and was waiting for
	// the lock behind the local end of a sign-out is served by it: the end counted as a wipe.
	@Test
	fun anInvalidationWithoutASessionIdWaitingBehindTheLocalEnd_isServedByIt() = runTest {
		val rig = SignOutRig(this)
		rig.holdTheWipe()

		launch { rig.signOut.execute(Unit).toList() }
		runCurrent()
		assertTrue(rig.wipeStarted.isCompleted, "the sign-out is wiping the database")

		val waiting = launch { rig.recovery.invalidateSession(sessionId = null) }
		runCurrent()
		rig.releaseTheWipe()
		advanceUntilIdle()

		assertTrue(waiting.isCompleted)
		assertEquals(1, rig.application.clearCalls, "the session is wiped once")
		assertEquals(0, rig.invalidations.raised, "no signal after a manual sign-out")
		assertNull(rig.settings.sessionResetNotice, "no notice after a manual sign-out")
	}

	// A CancellationException that is not the job's, thrown from inside cancelActiveWork() (a child that
	// ignores its cancellation makes the pass time out and the report of it throws): one more failure of
	// that step, not the end of the sign-out.
	@Test
	fun aForeignCancellationInsideCancelActiveWork_doesNotCutTheSignOut() = runTest {
		val foreign = ForeignCancellationReporting()
		val rig = SignOutRig(this, scopeReporting = foreign)
		val never = CompletableDeferred<Unit>()
		rig.sessionScope.launch { withContext(NonCancellable) { never.await() } }
		runCurrent()

		launch { rig.signOut.execute(Unit).toList() }
		advanceUntilIdle()

		assertTrue(foreign.thrown > 0, "the foreign cancellation was thrown inside cancelActiveWork")
		assertTrue(rig.store.isGone(), "the session is gone")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "the mark is withdrawn")
		never.complete(Unit)
	}
}

private enum class LocalStep { CancelActiveWork, ClearData, ClearSession, ResetSyncStatus }

private enum class ReleaseAt { RevokeInFlight, CancelActiveWork, ClearData, ClearSession, ResetSyncStatus, AfterTheEnd }

private class SignOutRig(
	testScope: TestScope,
	scopeReporting: ReportingRepository? = null,
	endReporting: ReportingRepository? = null
) {
	val settings = FakeSettingsRepository()
	val invalidations = CountingSessionInvalidationRepository()
	val reporting = RecordingReportingRepository()
	val store = RealSessionStore(settings = settings, invalidations = invalidations).also { it.seed() }
	val wipeStarted = CompletableDeferred<Unit>()
	private var wipeGate: CompletableDeferred<Unit>? = null
	var onWipe: suspend () -> Unit = {}
	var onSessionClear: suspend () -> Unit = {}
	var onSessionCleared: suspend () -> Unit = {}
	var onSnapshotRead: suspend () -> Unit = {}
	var onSyncStatusReset: suspend () -> Unit = {}
	var onRefresh: suspend () -> RefreshTokens = { error("unused") }
	var onRevoke: suspend () -> Unit = {}
	var refreshCalls = 0
	var failSnapshotReads = false

	val credentials = FakeCredentialsRepository()

	private val dispatcher = StandardTestDispatcher(testScope.testScheduler)
	val sessionScope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher), scopeReporting)
	val application = ProductionLikeApplicationRepository(
		settings = settings,
		stores = listOf(store.active, store.legacy),
		memory = listOf(store.memory),
		// The wipe of the app empties the secure store the password lives in.
		database = {
			credentials.password = null
			onWipe()
			wipeGate?.let { gate ->
				wipeStarted.complete(Unit)
				gate.await()
			}
		}
	)
	private val attestation = object : AttestationRepository {
		override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
	}
	private val auth = object : AuthRepository {
		override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

		override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

		override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) =
			error("unused")

		override suspend fun refreshTokens(
			sessionId: String,
			refreshToken: String,
			attestation: Attestation
		): RefreshTokens {
			refreshCalls += 1
			return onRefresh()
		}

		override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
			onRevoke()
	}

	val recovery = SessionRecoveryDataSource(
		sessionRepository = object : SessionRepository by store.repository {
			override suspend fun getActiveSessionSnapshot(): SessionSnapshot? {
				onSnapshotRead()
				check(!failSnapshotReads) { "stored session unreadable" }
				return store.repository.getActiveSessionSnapshot()
			}

			override suspend fun clear() {
				onSessionClear()
				store.repository.clear()
				onSessionCleared()
			}
		},
		applicationRepository = application,
		sessionInvalidationRepository = invalidations,
		syncStatusRepository = object : SyncStatusRepository by FakeSyncStatusRepository() {
			override suspend fun reset() {
				onSyncStatusReset()
			}
		},
		attestationRepository = attestation,
		authRepository = auth,
		credentialsRepository = credentials,
		settingsRepository = settings,
		reportingRepository = endReporting ?: reporting,
		sessionCoroutineScope = sessionScope,
		appCoroutineScope = testAppCoroutineScope(dispatcher)
	)

	val signOut = SignOutUseCase(
		authRepository = auth,
		attestationRepository = attestation,
		sessionRepository = store.repository,
		sessionInvalidationRepository = invalidations,
		sessionEndRepository = recovery,
		reportingRepository = reporting
	)

	fun holdTheWipe() {
		wipeGate = CompletableDeferred()
	}

	fun releaseTheWipe() {
		wipeGate?.complete(Unit)
	}
}
