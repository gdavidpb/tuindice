package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncDataRepository(
	private val settingsDataSource: SyncSettingsLocalDataSource,
	private val remoteDataSource: SyncRemoteDataSource,
	syncDispatcher: CoroutineDispatcher = Dispatchers.Default
) : SyncRepository {
	private val syncScope = CoroutineScope(SupervisorJob() + syncDispatcher)
	private val syncMutex = Mutex()

	override fun scheduleSync(password: String) {
		syncScope.launch {
			runCatching {
				syncMutex.withLock {
					val isOnCooldown = settingsDataSource.isSyncOnCooldown()

					if (isOnCooldown) return@withLock

					remoteDataSource.sync(password)
					settingsDataSource.setSyncOnCooldown()
					settingsDataSource.clearFeatureCooldowns()
				}
			}
		}
	}
}
