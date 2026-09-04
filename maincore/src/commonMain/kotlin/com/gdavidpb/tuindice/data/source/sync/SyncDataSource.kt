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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Compared as a string on purpose: the contract documents `reason` as a string so a new value can
// never fail a parse.
private const val OUTDATED_CREDENTIALS_REASON = "OUTDATED_CREDENTIALS"

class SyncDataSource(
	private val settingsDataSource: SyncSettingsLocalDataRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val remoteDataSource: SyncRemoteDataRepository,
	private val recordLocalDataSource: AcademicRecordLocalDataRepository,
	private val userLocalDataSource: LocalDataRepository,
	private val coroutineScope: CoroutineScope
) : SyncRepository {
	private val syncMutex = Mutex()
	private val syncInProgress = MutableStateFlow(false)

	override fun observeSyncInProgress(): Flow<Boolean> = syncInProgress.asStateFlow()

	override fun scheduleSync(password: String, policy: SyncPolicy) {
		coroutineScope.launch {
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

					syncInProgress.value = true

					try {
						val syncResult = remoteDataSource.sync(password)

						recordLocalDataSource.saveAcademicRecord(syncResult.record)
						userLocalDataSource.updateUser(syncResult.user)
						syncStatusRepository.setLastSuccessfulSyncAt(syncResult.user.lastUpdate)
						syncStatusRepository.setSyncReport(syncResult.sync)
						syncStatusRepository.setSyncStatus(SyncStatus.Healthy)
						settingsDataSource.clearSyncRetryBackoff()
						settingsDataSource.setSyncOnCooldown()
						settingsDataSource.setSyncedFeatureCooldowns()
						settingsDataSource.clearStaleFeatureCooldowns()
					} finally {
						syncInProgress.value = false
					}
				}
			}.onFailure { throwable ->
				if (throwable is CancellationException) throw throwable

				markSyncFailure(throwable)
			}
		}
	}

	private suspend fun markSyncFailure(throwable: Throwable) {
		val syncHttpStatusCode = throwable.syncHttpStatusCode()
		val syncStatus = when {
			// A 409 is not always an expired password: the record can also be written by another
			// device while this sync was fetching. OutdatedCredentials is a latch — it stops syncing
			// and blocks the pending-changes flush until the user re-authenticates — so only the
			// reason the server actually names gets to set it. Any other reason, an unrecognized one,
			// or a body we could not read falls through to a plain failure the next sync can clear.
			(syncHttpStatusCode == HttpStatusCode.Conflict || throwable.isConflict()) &&
				throwable.syncConflictReason() == OUTDATED_CREDENTIALS_REASON ->
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
		if (syncStatus != SyncStatus.OutdatedCredentials) {
			runCatching {
				settingsDataSource.setSyncOnCooldown()
			}
		}
		if (isSyncRetryable) {
			runCatching {
				settingsDataSource.markSyncRetryBackoff(throwable)
			}
		}
	}

	private fun Throwable.syncHttpStatusCode(): HttpStatusCode? {
		return when (this) {
			is SyncRemoteException -> statusCode
			else -> null
		}
	}

	private fun Throwable.syncConflictReason(): String? {
		return when (this) {
			is SyncRemoteException -> conflictReason
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
