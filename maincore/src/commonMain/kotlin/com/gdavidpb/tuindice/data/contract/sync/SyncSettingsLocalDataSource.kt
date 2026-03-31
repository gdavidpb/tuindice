package com.gdavidpb.tuindice.data.contract.sync


interface SyncSettingsLocalDataSource {
	suspend fun isSyncOnCooldown(): Boolean
	suspend fun setSyncOnCooldown()
	suspend fun clearFeatureCooldowns()
}
