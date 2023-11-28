package com.gdavidpb.tuindice.record.data.repository.quarter

interface SettingsDataSource {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
}