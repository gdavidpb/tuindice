package com.gdavidpb.tuindice.data.source.application

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.CountingSessionInvalidationRepository
import com.gdavidpb.tuindice.testing.RealSessionStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The end of the session with the classes the iOS app really runs it with: the real session store over
// the real secure-store source and the in-memory mirror, and the real iOS wipe. A caller cancelled while
// the wipe runs must still find the end of the session whole: the session gone only together with its
// notice persisted and its signal emitted.
@OptIn(ExperimentalCoroutinesApi::class)
class IosSessionInvalidationRealWiringTest {
	@Test
	fun aCallerCancelledWhileTheDatabaseIsWiped_stillLeavesTheNoticeAndTheSignal() = runTest {
		val settings = FakeSettingsRepository()
		val signals = CountingSessionInvalidationRepository()
		val store = RealSessionStore(settings = settings, invalidations = signals)
		store.seed()
		val dispatcher = StandardTestDispatcher(testScheduler)
		lateinit var caller: Job
		val recovery = SessionRecoveryDataSource(
			sessionRepository = store.repository,
			applicationRepository = IosApplicationDataSource(
				persistenceMaintenanceRepository = object : PersistenceMaintenanceRepository {
					override suspend fun clearAll() {
						caller.cancel()
						yield()
					}
				},
				settingsRepository = settings,
				secureStore = store.active,
				legacySecureStore = store.legacy,
				attestationCapability = NoOpAttestationCapability(),
				externalActionsCapability = NoOpExternalActionsCapability(),
				sessionMemory = { listOf(store.memory) },
				sessionResidue = { emptyList() }
			),
			sessionInvalidationRepository = signals,
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = object : AttestationRepository {
				override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
			},
			authRepository = UnusedAuthRepository,
			credentialsRepository = FakeCredentialsRepository(),
			settingsRepository = settings,
			reportingRepository = RecordingReportingRepository(),
			sessionCoroutineScope = testSessionCoroutineScope(dispatcher),
			appCoroutineScope = testAppCoroutineScope(dispatcher)
		)

		caller = launch(start = CoroutineStart.UNDISPATCHED) {
			recovery.invalidateSession(sessionId = "session-old")
		}
		advanceUntilIdle()

		assertTrue(caller.isCancelled, "the caller was cancelled")
		assertTrue(store.isGone(), "the session is gone")
		assertEquals(SessionResetNoticeReason.ServerEndedSession, settings.sessionResetNoticeReason, "with its notice")
		assertEquals(1, signals.raised, "and its signal emitted once")
	}
}

private object UnusedAuthRepository : AuthRepository {
	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(sessionId: String, refreshToken: String, attestation: Attestation): RefreshTokens =
		error("unused")

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}
