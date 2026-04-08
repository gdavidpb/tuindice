package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptProjectionEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicAttemptProjectionTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicAttemptProjectionDao : UpsertDao<AcademicAttemptProjectionEntity>() {
	@Query("SELECT * FROM ${AcademicAttemptProjectionTable.TABLE_NAME} WHERE ${AcademicAttemptProjectionTable.RECORD_ID} = :recordId AND ${AcademicAttemptProjectionTable.VIEW_MODE} = :viewMode ORDER BY ${AcademicAttemptProjectionTable.TERM_ID} DESC, ${AcademicAttemptProjectionTable.SEQUENCE_IN_TERM} ASC, ${AcademicAttemptProjectionTable.ID} ASC")
	abstract fun observeAttemptsFlow(recordId: String, viewMode: String): Flow<List<AcademicAttemptProjectionEntity>>

	@Query("SELECT * FROM ${AcademicAttemptProjectionTable.TABLE_NAME} WHERE ${AcademicAttemptProjectionTable.RECORD_ID} = :recordId AND ${AcademicAttemptProjectionTable.VIEW_MODE} = :viewMode ORDER BY ${AcademicAttemptProjectionTable.TERM_ID} DESC, ${AcademicAttemptProjectionTable.SEQUENCE_IN_TERM} ASC, ${AcademicAttemptProjectionTable.ID} ASC")
	abstract suspend fun getAttempts(recordId: String, viewMode: String): List<AcademicAttemptProjectionEntity>

	@Query("DELETE FROM ${AcademicAttemptProjectionTable.TABLE_NAME} WHERE ${AcademicAttemptProjectionTable.RECORD_ID} = :recordId")
	abstract suspend fun deleteByRecord(recordId: String)

	@Query("DELETE FROM ${AcademicAttemptProjectionTable.TABLE_NAME} WHERE ${AcademicAttemptProjectionTable.RECORD_ID} = :recordId AND ${AcademicAttemptProjectionTable.VIEW_MODE} = :viewMode")
	abstract suspend fun deleteByRecordAndViewMode(recordId: String, viewMode: String)
}
