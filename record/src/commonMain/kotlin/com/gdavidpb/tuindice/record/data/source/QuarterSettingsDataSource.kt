package com.gdavidpb.tuindice.record.data.source

interface QuarterSettingsDataSource {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
	fun getSelectedQuarterId(): String?
	fun setSelectedQuarterId(quarterId: String)
}
