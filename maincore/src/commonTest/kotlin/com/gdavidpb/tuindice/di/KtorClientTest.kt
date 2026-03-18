package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class KtorClientTest {
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
		assertEquals(listOf(SyncStatus.Healthy), syncStatusRepository.setStatuses)
		assertTrue(applicationRepository.cleared)
		assertEquals(1, sessionInvalidationRepository.invalidationCalls)
	}
}
