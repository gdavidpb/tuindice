package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.domain.repository.CoreCacheStateRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleSyncUseCaseTest {
	@Test
	fun executeOnBackground_usesRespectCooldown_whenBaseCacheIsUsable() = runTest {
		val syncRepository = FakeSyncRepository()
		val useCase = createUseCase(
			syncRepository = syncRepository,
			coreCacheStateRepository = FakeCoreCacheStateRepository(requiresBaseRehydration = false)
		)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(listOf("stored-password"), syncRepository.scheduledSyncCalls)
		assertEquals(listOf(SyncPolicy.RespectCooldown), syncRepository.scheduledSyncPolicies)
	}

	@Test
	fun executeOnBackground_usesForceRefresh_whenBaseCacheRequiresRehydration() = runTest {
		val syncRepository = FakeSyncRepository()
		val useCase = createUseCase(
			syncRepository = syncRepository,
			coreCacheStateRepository = FakeCoreCacheStateRepository(requiresBaseRehydration = true)
		)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(listOf("stored-password"), syncRepository.scheduledSyncCalls)
		assertEquals(listOf(SyncPolicy.ForceRefresh), syncRepository.scheduledSyncPolicies)
	}

	@Test
	fun executeOnBackground_doesNotScheduleSync_whenSessionIsMissing() = runTest {
		val syncRepository = FakeSyncRepository()
		val useCase = createUseCase(
			sessionRepository = FakeSessionRepository(sessionId = ""),
			syncRepository = syncRepository,
			coreCacheStateRepository = FakeCoreCacheStateRepository(requiresBaseRehydration = true)
		)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(emptyList(), syncRepository.scheduledSyncCalls)
		assertEquals(emptyList(), syncRepository.scheduledSyncPolicies)
	}

	@Test
	fun executeOnBackground_latchesMissingCredentials_whenPasswordIsMissing() = runTest {
		val syncRepository = FakeSyncRepository()
		val syncStatusRepository = FakeSyncStatusRepository()
		val useCase = createUseCase(
			credentialsRepository = FakeCredentialsRepository(password = null),
			syncRepository = syncRepository,
			syncStatusRepository = syncStatusRepository,
			coreCacheStateRepository = FakeCoreCacheStateRepository(requiresBaseRehydration = true)
		)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(emptyList(), syncRepository.scheduledSyncCalls)
		assertEquals(emptyList(), syncRepository.scheduledSyncPolicies)
		assertEquals(listOf(SyncStatus.MissingCredentials), syncStatusRepository.setStatuses)
	}

	@Test
	fun executeOnBackground_keepsOutdatedCredentials_whenPasswordIsMissing() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val useCase = createUseCase(
			credentialsRepository = FakeCredentialsRepository(password = null),
			syncStatusRepository = syncStatusRepository
		)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(emptyList(), syncStatusRepository.setStatuses)
	}

	@Test
	fun executeOnBackground_doesNotLatch_whenSessionIsMissing() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository()
		val useCase = createUseCase(
			sessionRepository = FakeSessionRepository(sessionId = ""),
			credentialsRepository = FakeCredentialsRepository(password = null),
			syncStatusRepository = syncStatusRepository
		)

		useCase.executeOnBackground(Unit).toList()

		assertEquals(emptyList(), syncStatusRepository.setStatuses)
	}

	private fun createUseCase(
		sessionRepository: FakeSessionRepository = FakeSessionRepository(),
		credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(password = "stored-password"),
		syncRepository: FakeSyncRepository = FakeSyncRepository(),
		syncStatusRepository: FakeSyncStatusRepository = FakeSyncStatusRepository(),
		coreCacheStateRepository: CoreCacheStateRepository = FakeCoreCacheStateRepository(),
	) = ScheduleSyncUseCase(
		sessionRepository = sessionRepository,
		credentialsRepository = credentialsRepository,
		syncRepository = syncRepository,
		syncStatusRepository = syncStatusRepository,
		coreCacheStateRepository = coreCacheStateRepository,
		reportingRepository = RecordingReportingRepository()
	)
}

private class FakeCoreCacheStateRepository(
	private val requiresBaseRehydration: Boolean = false
) : CoreCacheStateRepository {
	override suspend fun requiresBaseRehydration(): Boolean = requiresBaseRehydration
}
