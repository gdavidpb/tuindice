package com.gdavidpb.tuindice.record.data.quarter.source

interface SettingsDataSource {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
}