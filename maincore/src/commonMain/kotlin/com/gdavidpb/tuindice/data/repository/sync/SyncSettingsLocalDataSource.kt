package com.gdavidpb.tuindice.data.repository.sync

interface SyncSettingsLocalDataSource {
	suspend fun isSyncOnCooldown(): Boolean
	suspend fun setSyncOnCooldown()
	suspend fun clearFeatureCooldowns()
}
