package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isFailedDependency
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.base.utils.extension.isSyncRetryable
import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncSettingsLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import io.ktor.http.HttpStatusCode
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

	override fun scheduleSync(password: String, policy: SyncPolicy) {
		syncScope.launch {
			runCatching {
				syncMutex.withLock {
					if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials)
						return@withLock

					val isOnCooldown = settingsDataSource.isSyncOnCooldown()
					val isSyncRetryBackoffActive = settingsDataSource.isSyncRetryBackoffActive()

					if ((isOnCooldown || isSyncRetryBackoffActive) && policy == SyncPolicy.RespectCooldown) return@withLock
					if (policy == SyncPolicy.ForceRefresh) {
						settingsDataSource.clearRecoveryCooldowns()
						settingsDataSource.clearSyncRetryBackoff()
					}

					val syncResult = try {
						syncInProgress.value = true
						remoteDataSource.sync(password)
					} finally {
						syncInProgress.value = false
					}

					recordLocalDataSource.saveAcademicRecord(syncResult.record)
					userLocalDataSource.updateUser(syncResult.user)
					syncStatusRepository.setLastSuccessfulSyncAt(syncResult.user.lastUpdate)
					syncStatusRepository.setSyncReport(syncResult.sync)
					syncStatusRepository.setSyncStatus(SyncStatus.Healthy)
					settingsDataSource.clearSyncRetryBackoff()
					settingsDataSource.setSyncOnCooldown()
					settingsDataSource.setSyncedFeatureCooldowns()
					settingsDataSource.clearStaleFeatureCooldowns()
				}
			}.onFailure { throwable ->
				val syncHttpStatusCode = throwable.syncHttpStatusCode()
				val syncStatus = when {
					syncHttpStatusCode == HttpStatusCode.Conflict || throwable.isConflict() ->
						SyncStatus.OutdatedCredentials

					syncHttpStatusCode == HttpStatusCode.ServiceUnavailable ||
						syncHttpStatusCode == HttpStatusCode.FailedDependency ||
						throwable.isUnavailable() ||
						throwable.isFailedDependency() ->
						SyncStatus.Unavailable

					else ->
						SyncStatus.Failed
				}
				val isSyncRetryable = throwable.isSyncRetryable(syncHttpStatusCode)

				runCatching {
					syncStatusRepository.setSyncReport(throwable.syncReportOrDefault())
					syncStatusRepository.setSyncStatus(syncStatus)
				}
				if (isSyncRetryable) {
					runCatching {
						settingsDataSource.markSyncRetryBackoff(throwable)
					}
				}
			}
		}
	}

	private fun Throwable.syncHttpStatusCode(): HttpStatusCode? {
		return when (this) {
			is SyncRemoteException -> statusCode
			else -> null
		}
	}

	private fun Throwable.syncReportOrDefault(): SyncReport {
		return when (this) {
			is SyncRemoteException -> syncReport
			else -> null
		} ?: SyncReport.success()
	}

	private fun Throwable.isSyncRetryable(statusCode: HttpStatusCode?): Boolean {
		return when {
			statusCode == HttpStatusCode.ServiceUnavailable ||
				statusCode == HttpStatusCode.FailedDependency ||
				statusCode == HttpStatusCode.TooManyRequests ||
				statusCode?.value?.let { value -> value in 500..599 } == true ->
				true

			this is SyncRemoteException ->
				cause?.isSyncRetryable() == true

			else ->
				isSyncRetryable()
		}
	}
}
