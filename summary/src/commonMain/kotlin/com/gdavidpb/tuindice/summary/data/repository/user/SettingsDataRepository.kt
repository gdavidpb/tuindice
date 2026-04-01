package com.gdavidpb.tuindice.summary.data.repository.user


interface SettingsDataRepository {
	suspend fun isGetUserOnCooldown(): Boolean
	suspend fun setGetUserOnCooldown()
}