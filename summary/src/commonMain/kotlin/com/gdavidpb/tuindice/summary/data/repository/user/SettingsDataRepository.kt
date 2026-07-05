package com.gdavidpb.tuindice.summary.data.repository.user

import kotlinx.coroutines.flow.Flow

interface SettingsDataRepository {
	suspend fun isGetUserOnCooldown(): Boolean
	suspend fun setGetUserOnCooldown()
	fun observeProfilePictureVersion(): Flow<Int>
	suspend fun bumpProfilePictureVersion()
}