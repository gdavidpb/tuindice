package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
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
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
}

private enum class LocalStep { CancelActiveWork, ClearData, ClearSession, ResetSyncStatus }

private class SignOutRig(testScope: TestScope) {
	val settings = FakeSettingsRepository()
	val invalidations = CountingSessionInvalidationRepository()
	val store = RealSessionStore(settings = settings, invalidations = invalidations).also { it.seed() }
	val wipeStarted = CompletableDeferred<Unit>()
	private var wipeGate: CompletableDeferred<Unit>? = null
	var onWipe: suspend () -> Unit = {}
	var onSessionClear: suspend () -> Unit = {}
	var onSyncStatusReset: suspend () -> Unit = {}

	private val dispatcher = StandardTestDispatcher(testScope.testScheduler)
	val sessionScope: SessionCoroutineScope = testSessionCoroutineScope(dispatcher)
	private val application = ProductionLikeApplicationRepository(
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
		): RefreshTokens = error("unused")

		override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) = Unit
	}

	val signOut = SignOutUseCase(
		authRepository = auth,
		attestationRepository = attestation,
		sessionRepository = object : SessionRepository by store.repository {
			override suspend fun clear() {
				onSessionClear()
				store.repository.clear()
			}
		},
		sessionInvalidationRepository = invalidations,
		applicationRepository = application,
		syncStatusRepository = object : SyncStatusRepository by FakeSyncStatusRepository() {
			override suspend fun reset() {
				onSyncStatusReset()
			}
		},
		sessionCoroutineScope = sessionScope,
		appCoroutineScope = testAppCoroutineScope(dispatcher),
		reportingRepository = RecordingReportingRepository()
	)

	val recovery = SessionRecoveryDataSource(
		sessionRepository = store.repository,
		applicationRepository = application,
		sessionInvalidationRepository = invalidations,
		syncStatusRepository = FakeSyncStatusRepository(),
		attestationRepository = attestation,
		authRepository = auth,
		credentialsRepository = FakeCredentialsRepository(),
		settingsRepository = settings,
		reportingRepository = RecordingReportingRepository(),
		sessionCoroutineScope = sessionScope,
		appCoroutineScope = testAppCoroutineScope(dispatcher)
	)

	fun holdTheWipe() {
		wipeGate = CompletableDeferred()
	}

	fun releaseTheWipe() {
		wipeGate?.complete(Unit)
	}
}
