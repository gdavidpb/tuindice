package com.gdavidpb.tuindice.data.repository.sync


interface SyncSettingsLocalDataRepository {
	suspend fun isSyncOnCooldown(): Boolean
	suspend fun setSyncOnCooldown()
	suspend fun setSyncedFeatureCooldowns()
	suspend fun clearStaleFeatureCooldowns()
}
