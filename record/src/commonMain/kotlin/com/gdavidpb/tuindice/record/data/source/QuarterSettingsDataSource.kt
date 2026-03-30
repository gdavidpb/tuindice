package com.gdavidpb.tuindice.record.data.source

interface QuarterSettingsDataSource {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
}
