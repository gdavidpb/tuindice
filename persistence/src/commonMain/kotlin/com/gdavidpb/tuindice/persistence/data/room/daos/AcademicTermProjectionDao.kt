package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermProjectionEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicTermProjectionTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicTermProjectionDao : UpsertDao<AcademicTermProjectionEntity>() {
	@Query("SELECT * FROM ${AcademicTermProjectionTable.TABLE_NAME} WHERE ${AcademicTermProjectionTable.RECORD_ID} = :recordId AND ${AcademicTermProjectionTable.VIEW_MODE} = :viewMode ORDER BY ${AcademicTermProjectionTable.START_AT} DESC, ${AcademicTermProjectionTable.ID} ASC")
	abstract fun observeTermsFlow(recordId: String, viewMode: String): Flow<List<AcademicTermProjectionEntity>>

	@Query("SELECT * FROM ${AcademicTermProjectionTable.TABLE_NAME} WHERE ${AcademicTermProjectionTable.RECORD_ID} = :recordId AND ${AcademicTermProjectionTable.VIEW_MODE} = :viewMode ORDER BY ${AcademicTermProjectionTable.START_AT} DESC, ${AcademicTermProjectionTable.ID} ASC")
	abstract suspend fun getTerms(recordId: String, viewMode: String): List<AcademicTermProjectionEntity>

	@Query("DELETE FROM ${AcademicTermProjectionTable.TABLE_NAME} WHERE ${AcademicTermProjectionTable.RECORD_ID} = :recordId")
	abstract suspend fun deleteByRecord(recordId: String)

	@Query("DELETE FROM ${AcademicTermProjectionTable.TABLE_NAME} WHERE ${AcademicTermProjectionTable.RECORD_ID} = :recordId AND ${AcademicTermProjectionTable.VIEW_MODE} = :viewMode")
	abstract suspend fun deleteByRecordAndViewMode(recordId: String, viewMode: String)
}
