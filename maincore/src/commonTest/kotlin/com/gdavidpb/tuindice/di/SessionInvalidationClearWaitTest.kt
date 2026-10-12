package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The one step of the invalidation that can really wait is the session store (it waits for its own
// lock, as SessionDataSource.clear does). A caller cancelled while it waits there has not changed
// anything yet: the wipe of the data is done, the session is still valid, and nothing was announced.
@OptIn(ExperimentalCoroutinesApi::class)
class SessionInvalidationClearWaitTest {
	@Test
	fun aCallerCancelledWhileTheSessionStoreWaitsForItsLock_leavesTheSessionAsItWas_andALaterInvalidationCompletesIt() =
		runTest {
			val store = FakeSessionRepository(
				sessionId = "session-old",
				usbId = "12-34567",
				accessToken = "access-old",
				refreshToken = "refresh-old"
			)
			val lock = Mutex()
			val application = RecordingApplicationRepository()
			val invalidations = FakeSessionInvalidationRepository()
			val settings = FakeSettingsRepository()
			val reporting = RecordingReportingRepository()
			val recovery = SessionRecoveryDataSource(
				sessionRepository = LockedClearSessionRepository(store, lock),
				applicationRepository = application,
				sessionInvalidationRepository = invalidations,
				syncStatusRepository = FakeSyncStatusRepository(),
				attestationRepository = object : AttestationRepository {
					override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
				},
				authRepository = ClearWaitUnusedAuthRepository,
				credentialsRepository = FakeCredentialsRepository(),
				settingsRepository = settings,
				reportingRepository = reporting,
				sessionCoroutineScope = testSessionCoroutineScope(StandardTestDispatcher(testScheduler))
			)

			// Somebody holds the lock of the session store (a token read in flight).
			lock.lock()
			val first = launch { recovery.invalidateSession(sessionId = "session-old") }
			runCurrent()
			assertEquals(1, application.clearCalls, "the data was wiped and the caller waits for the store")

			first.cancel()
			runCurrent()

			assertTrue(first.isCancelled)
			assertFalse(store.cleared, "the session was not cleared")
			assertFalse(settings.sessionResetNoticePending, "no notice is persisted")
			assertEquals(0, invalidations.invalidationCalls, "and no signal is raised")
			assertEquals(emptyList(), reporting.loggedExceptions, "nor is a cause reported")

			// The lock is free again: the next request meets the rejection and invalidates.
			lock.unlock()
			launch { recovery.invalidateSession(sessionId = "session-old") }
			advanceUntilIdle()

			assertEquals(2, application.clearCalls)
			assertTrue(store.cleared)
			assertTrue(settings.sessionResetNoticePending)
			assertEquals(1, invalidations.invalidationCalls, "announced exactly once")
			assertEquals(1, reporting.loggedExceptions.size)
		}
}

// A session store whose clear waits for a lock first, as the real one does.
private class LockedClearSessionRepository(
	private val delegate: FakeSessionRepository,
	private val lock: Mutex
) : SessionRepository by delegate {
	override suspend fun clear() {
		lock.withLock { delegate.clear() }
	}
}

private object ClearWaitUnusedAuthRepository : AuthRepository {
	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(sessionId: String, refreshToken: String, attestation: Attestation): RefreshTokens =
		error("unused")

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}
