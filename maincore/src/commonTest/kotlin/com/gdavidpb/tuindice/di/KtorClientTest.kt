package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class KtorClientTest {
	@Test
	fun isSessionInvalidatingRefreshFailure_matchesUnauthorizedForbiddenAndLocked() {
		assertTrue(clientRequestException(HttpStatusCode.Unauthorized).isSessionInvalidatingRefreshFailure())
		assertTrue(clientRequestException(HttpStatusCode.Forbidden).isSessionInvalidatingRefreshFailure())
		assertTrue(clientRequestException(HttpStatusCode.Locked).isSessionInvalidatingRefreshFailure())
		assertFalse(clientRequestException(HttpStatusCode.ServiceUnavailable).isSessionInvalidatingRefreshFailure())
	}

	@Test
	fun handleUnauthorizedTokenRefresh_clearsLocalSessionState_andEmitsInvalidation() = runTest {
		val sessionRepository = FakeSessionRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val applicationRepository = RecordingApplicationRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()

		handleUnauthorizedTokenRefresh(
			sessionRepository = sessionRepository,
			syncStatusRepository = syncStatusRepository,
			applicationRepository = applicationRepository,
			sessionInvalidationRepository = sessionInvalidationRepository
		)

		assertTrue(sessionRepository.cleared)
		assertEquals(1, syncStatusRepository.resetCalls)
		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertTrue(applicationRepository.cleared)
		assertEquals(1, sessionInvalidationRepository.invalidationCalls)
	}
}
