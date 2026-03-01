package com.gdavidpb.tuindice.record.data.repository

interface QuarterSettingsDataSource {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
}
