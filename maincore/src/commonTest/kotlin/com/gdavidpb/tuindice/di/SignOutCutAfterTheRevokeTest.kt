package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
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
	@Test
	fun aLocalStepThatFailsAfterTheRevoke_stillEndsTheSignOutAndReportsTheFailure() = runTest {
		val rig = SignOutRig(this)
		rig.onWipe = { error("disk failure") }

		val states = rig.signOut.execute(Unit).toList()
		advanceUntilIdle()

		assertTrue(states.last() is UseCaseState.Data, "the caller is told the sign-out is done")
		assertTrue(rig.store.isGone(), "the session is absent")
		assertEquals(0, rig.invalidations.raised, "nobody is told the session was closed")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark is withdrawn")
		assertTrue(rig.reporting.loggedExceptions.any { it.message == "disk failure" }, "the failure is reported")
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
	// sign-out runs afterwards, so the device is wiped by both and the user is told by neither.
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
		assertEquals(0, rig.invalidations.raised, "no signal after a manual sign-out")
		assertNull(rig.settings.sessionResetNotice, "no notice after a manual sign-out")
		assertTrue(rig.invalidations.notifySessionInvalidated(sessionId = null), "and the mark is not left behind")
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

private class SignOutRig(testScope: TestScope, scopeReporting: ReportingRepository? = null) {
	val settings = FakeSettingsRepository()
	val invalidations = CountingSessionInvalidationRepository()
	val reporting = RecordingReportingRepository()
	val store = RealSessionStore(settings = settings, invalidations = invalidations).also { it.seed() }
	val wipeStarted = CompletableDeferred<Unit>()
	private var wipeGate: CompletableDeferred<Unit>? = null
	var onWipe: suspend () -> Unit = {}
	var onSessionClear: suspend () -> Unit = {}
	var onSyncStatusReset: suspend () -> Unit = {}
	var onRefresh: suspend () -> RefreshTokens = { error("unused") }
	var onRevoke: suspend () -> Unit = {}
	var refreshCalls = 0

	private val dispatcher = StandardTestDispatcher(testScope.testScheduler)
	val sessionScope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher), scopeReporting)
	val application = ProductionLikeApplicationRepository(
		settings = settings,
		stores = listOf(store.active, store.legacy),
		memory = listOf(store.memory),
		database = {
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
			override suspend fun clear() {
				onSessionClear()
				store.repository.clear()
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
		credentialsRepository = FakeCredentialsRepository(),
		settingsRepository = settings,
		reportingRepository = reporting,
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
