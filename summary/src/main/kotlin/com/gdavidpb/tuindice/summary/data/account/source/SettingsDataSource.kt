package com.gdavidpb.tuindice.summary.data.account.source

interface SettingsDataSource {
	suspend fun isGetAccountOnCooldown(): Boolean
	suspend fun setGetAccountOnCooldown()
}