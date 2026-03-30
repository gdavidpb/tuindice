package com.gdavidpb.tuindice.data.source.sync

interface SyncSettingsLocalDataSource {
	suspend fun isSyncOnCooldown(): Boolean
	suspend fun setSyncOnCooldown()
	suspend fun clearFeatureCooldowns()
}
