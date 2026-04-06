package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SyncRepositoryContractTest {
	@Test
	fun scheduleSync_callsApi_marksCooldown_and_clearsFeatureCooldowns() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.Failed)
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123")
		advanceUntilIdle()

		assertEquals(listOf("secret123"), remoteDataSource.syncPasswords)
		assertEquals(true, settingsDataSource.cooldownMarked)
		assertEquals(true, settingsDataSource.featureCooldownsCleared)
		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Healthy), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_skipsApiWhenOnCooldown() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = true)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret")
		advanceUntilIdle()

		assertEquals(emptyList(), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
	}

	@Test
	fun scheduleSync_ignoresConflictAndKeepsCooldownUntouched() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = clientRequestException(
				statusCode = HttpStatusCode.Conflict,
				path = "/sync/v1"
			)
		)
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "stored-secret")
		advanceUntilIdle()

		assertEquals(listOf("stored-secret"), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
		assertEquals(false, settingsDataSource.featureCooldownsCleared)
		assertEquals(SyncStatus.OutdatedCredentials, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.OutdatedCredentials), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_marksUnavailable_whenSyncFailsWithServiceUnavailable() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = clientRequestException(
				statusCode = HttpStatusCode.ServiceUnavailable,
				path = "/sync/v1"
			)
		)
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret")
		advanceUntilIdle()

		assertEquals(listOf("new-secret"), remoteDataSource.syncPasswords)
		assertEquals(SyncStatus.Unavailable, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Unavailable), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_marksUnavailable_whenSyncFailsWithFailedDependency() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = clientRequestException(
				statusCode = HttpStatusCode.FailedDependency,
				path = "/sync/v1"
			)
		)
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret")
		advanceUntilIdle()

		assertEquals(listOf("new-secret"), remoteDataSource.syncPasswords)
		assertEquals(SyncStatus.Unavailable, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Unavailable), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_marksFailed_whenSyncFailsWithUnhandledError() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = clientRequestException(
				statusCode = HttpStatusCode.InternalServerError,
				path = "/sync/v1"
			)
		)
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret")
		advanceUntilIdle()

		assertEquals(listOf("new-secret"), remoteDataSource.syncPasswords)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Failed), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_skipsApiWhenSyncStatusIsOutdatedCredentials() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "new-secret")
		advanceUntilIdle()

		assertEquals(emptyList(), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
	}
}

private class FakeSyncSettingsLocalDataSource(
	private val onCooldown: Boolean
) : SyncSettingsLocalDataRepository {
	var cooldownMarked = false
	var featureCooldownsCleared = false

	override suspend fun isSyncOnCooldown(): Boolean = onCooldown

	override suspend fun setSyncOnCooldown() {
		cooldownMarked = true
	}

	override suspend fun clearFeatureCooldowns() {
		featureCooldownsCleared = true
	}
}

private class FakeSyncStatusRepository(
	initialValue: SyncStatus = SyncStatus.Healthy
) : SyncStatusRepository {
	private val syncStatus = MutableStateFlow(initialValue)
	val setStatuses = mutableListOf<SyncStatus>()

	override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus

	override suspend fun getSyncStatus(): SyncStatus = syncStatus.value

	override suspend fun setSyncStatus(status: SyncStatus) {
		syncStatus.value = status
		setStatuses += status
	}

	override suspend fun reset() {
		syncStatus.value = SyncStatus.Healthy
	}
}

private class FakeSyncRemoteDataSource(
	private val throwable: Throwable? = null
) : SyncRemoteDataRepository {
	val syncPasswords = mutableListOf<String>()

	override suspend fun sync(password: String) {
		syncPasswords += password
		throwable?.let { throw it }
	}
}
