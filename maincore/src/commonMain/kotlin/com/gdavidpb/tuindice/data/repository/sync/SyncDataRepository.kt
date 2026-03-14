package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.base.domain.repository.OutdatedCredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncDataRepository(
	private val settingsDataSource: SyncSettingsLocalDataSource,
	private val outdatedCredentialsRepository: OutdatedCredentialsRepository,
	private val remoteDataSource: SyncRemoteDataSource,
	syncDispatcher: CoroutineDispatcher = Dispatchers.Default
) : SyncRepository {
	private val syncScope = CoroutineScope(SupervisorJob() + syncDispatcher)
	private val syncMutex = Mutex()

	override fun scheduleSync(password: String) {
		syncScope.launch {
			runCatching {
				syncMutex.withLock {
					if (outdatedCredentialsRepository.hasOutdatedCredentials()) return@withLock

					val isOnCooldown = settingsDataSource.isSyncOnCooldown()

					if (isOnCooldown) return@withLock

					runCatching {
						remoteDataSource.sync(password)
					}.onSuccess {
						outdatedCredentialsRepository.clearOutdatedCredentials()
						settingsDataSource.setSyncOnCooldown()
						settingsDataSource.clearFeatureCooldowns()
					}.onFailure { throwable ->
						if (throwable.isConflict()) {
							outdatedCredentialsRepository.setOutdatedCredentials()
						}
					}
				}
			}
		}
	}
}
