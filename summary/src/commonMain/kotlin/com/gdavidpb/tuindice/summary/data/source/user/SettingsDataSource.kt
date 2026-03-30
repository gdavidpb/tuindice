package com.gdavidpb.tuindice.summary.data.source.user

interface SettingsDataSource {
	suspend fun isGetUserOnCooldown(): Boolean
	suspend fun setGetUserOnCooldown()
}