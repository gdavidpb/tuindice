package com.gdavidpb.tuindice.summary.data.repository.user

interface SettingsDataSource {
	suspend fun isGetUserOnCooldown(): Boolean
	suspend fun setGetUserOnCooldown()
}