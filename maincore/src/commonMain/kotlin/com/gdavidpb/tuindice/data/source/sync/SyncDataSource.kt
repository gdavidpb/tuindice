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

// The reasons the server names on a 409 that are not about credentials. Compared as strings on
// purpose: the contract documents `reason` as a string so a new value can never fail a parse.
private const val CONCURRENT_WRITE_REASON = "CONCURRENT_WRITE"
private const val STALE_PRECONDITION_REASON = "STALE_PRECONDITION"

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
			// device while this sync was fetching. Telling that user their credentials expired sends
			// them to re-enter a password that was never the problem, so only the reason the server
			// names as OUTDATED_CREDENTIALS gets that message. Anything else — a concurrent write, a
			// stale precondition, a server too old to send a reason at all — is a plain failure the
			// next sync can clear.
			(syncHttpStatusCode == HttpStatusCode.Conflict || throwable.isConflict()) &&
				throwable.syncConflictReason() != CONCURRENT_WRITE_REASON &&
				throwable.syncConflictReason() != STALE_PRECONDITION_REASON ->
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
