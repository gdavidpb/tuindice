package com.gdavidpb.tuindice.record.data.repository


interface QuarterSettingsDataRepository {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
	fun getSelectedQuarterId(): String?
	fun setSelectedQuarterId(quarterId: String)
}
