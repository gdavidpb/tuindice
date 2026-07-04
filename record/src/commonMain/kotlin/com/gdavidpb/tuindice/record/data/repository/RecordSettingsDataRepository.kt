package com.gdavidpb.tuindice.record.data.repository

interface RecordSettingsDataRepository {
	suspend fun isGetAcademicRecordOnCooldown(): Boolean
	suspend fun setGetAcademicRecordOnCooldown()
}
