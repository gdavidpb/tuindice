package com.gdavidpb.tuindice.data.repository.sync

data class SyncRetryBackoffState(
	val retryCount: Int,
	val lastRetryAt: Long,
	val retryBackoffUntil: Long
)

interface SyncSettingsLocalDataRepository {
	suspend fun isSyncOnCooldown(): Boolean
	suspend fun setSyncOnCooldown()
	suspend fun setSyncedFeatureCooldowns()
	suspend fun clearStaleFeatureCooldowns()
	suspend fun clearRecoveryCooldowns()
	suspend fun isSyncRetryBackoffActive(): Boolean
	suspend fun markSyncRetryBackoff(throwable: Throwable)
	suspend fun clearSyncRetryBackoff()
	suspend fun getSyncRetryBackoffState(): SyncRetryBackoffState
}
