package com.gdavidpb.tuindice.summary.data.contract.user


interface SettingsDataSource {
	suspend fun isGetUserOnCooldown(): Boolean
	suspend fun setGetUserOnCooldown()
}