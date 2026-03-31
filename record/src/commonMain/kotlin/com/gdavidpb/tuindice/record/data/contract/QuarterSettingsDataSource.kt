package com.gdavidpb.tuindice.record.data.contract


interface QuarterSettingsDataSource {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
	fun getSelectedQuarterId(): String?
	fun setSelectedQuarterId(quarterId: String)
}
