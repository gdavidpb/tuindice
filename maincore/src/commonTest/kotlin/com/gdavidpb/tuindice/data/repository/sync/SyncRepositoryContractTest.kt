package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.testkit.base.repository.FakeOutdatedCredentialsRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SyncRepositoryContractTest {
	@Test
	fun scheduleSync_callsApi_marksCooldown_and_clearsFeatureCooldowns() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val outdatedCredentialsRepository = FakeOutdatedCredentialsRepository()
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = SyncDataRepository(
			settingsDataSource = settingsDataSource,
			outdatedCredentialsRepository = outdatedCredentialsRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "secret123")
		advanceUntilIdle()

		assertEquals(listOf("secret123"), remoteDataSource.syncPasswords)
		assertEquals(true, settingsDataSource.cooldownMarked)
		assertEquals(true, settingsDataSource.featureCooldownsCleared)
		assertEquals(false, outdatedCredentialsRepository.hasOutdatedCredentials())
		assertEquals(1, outdatedCredentialsRepository.clearCalls)
	}

	@Test
	fun scheduleSync_skipsApiWhenOnCooldown() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = true)
		val outdatedCredentialsRepository = FakeOutdatedCredentialsRepository()
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = SyncDataRepository(
			settingsDataSource = settingsDataSource,
			outdatedCredentialsRepository = outdatedCredentialsRepository,
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
		val outdatedCredentialsRepository = FakeOutdatedCredentialsRepository()
		val remoteDataSource = FakeSyncRemoteDataSource(
			throwable = clientRequestException(
				statusCode = HttpStatusCode.Conflict,
				path = "/sync/v1"
			)
		)
		val repository = SyncDataRepository(
			settingsDataSource = settingsDataSource,
			outdatedCredentialsRepository = outdatedCredentialsRepository,
			remoteDataSource = remoteDataSource,
			syncDispatcher = StandardTestDispatcher(testScheduler)
		)

		repository.scheduleSync(password = "stored-secret")
		advanceUntilIdle()

		assertEquals(listOf("stored-secret"), remoteDataSource.syncPasswords)
		assertEquals(false, settingsDataSource.cooldownMarked)
		assertEquals(false, settingsDataSource.featureCooldownsCleared)
		assertEquals(true, outdatedCredentialsRepository.hasOutdatedCredentials())
		assertEquals(1, outdatedCredentialsRepository.setCalls)
	}

	@Test
	fun scheduleSync_skipsApiWhenCredentialsAreMarkedAsOutdated() = runTest {
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val outdatedCredentialsRepository = FakeOutdatedCredentialsRepository(initialValue = true)
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = SyncDataRepository(
			settingsDataSource = settingsDataSource,
			outdatedCredentialsRepository = outdatedCredentialsRepository,
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
) : SyncSettingsLocalDataSource {
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

private class FakeSyncRemoteDataSource(
	private val throwable: Throwable? = null
) : SyncRemoteDataSource {
	val syncPasswords = mutableListOf<String>()

	override suspend fun sync(password: String) {
		syncPasswords += password
		throwable?.let { throw it }
	}
}
