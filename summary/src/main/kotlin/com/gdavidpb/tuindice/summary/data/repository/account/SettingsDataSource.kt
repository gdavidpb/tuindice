package com.gdavidpb.tuindice.summary.data.repository.account

interface SettingsDataSource {
	suspend fun isGetAccountOnCooldown(): Boolean
	suspend fun setGetAccountOnCooldown()
}