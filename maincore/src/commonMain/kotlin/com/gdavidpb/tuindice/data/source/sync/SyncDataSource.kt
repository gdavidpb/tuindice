package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isFailedDependency
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncSettingsLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncDataSource(
	private val settingsDataSource: SyncSettingsLocalDataRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val remoteDataSource: SyncRemoteDataRepository,
	private val recordLocalDataSource: AcademicRecordLocalDataRepository,
	private val userLocalDataSource: LocalDataRepository,
	syncDispatcher: CoroutineDispatcher = Dispatchers.Default
) : SyncRepository {
	private val syncScope = CoroutineScope(SupervisorJob() + syncDispatcher)
	private val syncMutex = Mutex()
	private val syncInProgress = MutableStateFlow(false)

	override fun observeSyncInProgress(): Flow<Boolean> = syncInProgress.asStateFlow()

	override fun scheduleSync(password: String) {
		syncScope.launch {
			runCatching {
				syncMutex.withLock {
					if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials)
						return@withLock

					val isOnCooldown = settingsDataSource.isSyncOnCooldown()

					if (isOnCooldown) return@withLock

					val syncResult = try {
						syncInProgress.value = true
						remoteDataSource.sync(password)
					} finally {
						syncInProgress.value = false
					}

					recordLocalDataSource.saveAcademicRecord(syncResult.record)
					userLocalDataSource.updateUser(syncResult.user)
					syncStatusRepository.setSyncStatus(SyncStatus.Healthy)
					settingsDataSource.setSyncOnCooldown()
					settingsDataSource.setSyncedFeatureCooldowns()
					settingsDataSource.clearStaleFeatureCooldowns()
				}
			}.onFailure { throwable ->
				val syncStatus = when {
					throwable.isConflict() ->
						SyncStatus.OutdatedCredentials

					throwable.isUnavailable() || throwable.isFailedDependency() ->
						SyncStatus.Unavailable

					else ->
						SyncStatus.Failed
				}

				runCatching {
					syncStatusRepository.setSyncStatus(syncStatus)
				}
			}
		}
	}
}
