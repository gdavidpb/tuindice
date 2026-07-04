package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.data.model.SyncRetryBackoffState

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
