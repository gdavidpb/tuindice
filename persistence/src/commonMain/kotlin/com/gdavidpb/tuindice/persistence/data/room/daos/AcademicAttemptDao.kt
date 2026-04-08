package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicAttemptTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicAttemptDao : UpsertDao<AcademicAttemptEntity>() {
	@Query("SELECT * FROM ${AcademicAttemptTable.TABLE_NAME} WHERE ${AcademicAttemptTable.RECORD_ID} = :recordId ORDER BY ${AcademicAttemptTable.TERM_ID} DESC, ${AcademicAttemptTable.SEQUENCE_IN_TERM} ASC, ${AcademicAttemptTable.ID} ASC")
	abstract fun observeAttemptsFlow(recordId: String): Flow<List<AcademicAttemptEntity>>

	@Query("SELECT * FROM ${AcademicAttemptTable.TABLE_NAME} WHERE ${AcademicAttemptTable.RECORD_ID} = :recordId ORDER BY ${AcademicAttemptTable.TERM_ID} DESC, ${AcademicAttemptTable.SEQUENCE_IN_TERM} ASC, ${AcademicAttemptTable.ID} ASC")
	abstract suspend fun getAttempts(recordId: String): List<AcademicAttemptEntity>

	@Query("DELETE FROM ${AcademicAttemptTable.TABLE_NAME} WHERE ${AcademicAttemptTable.RECORD_ID} = :recordId")
	abstract suspend fun deleteByRecord(recordId: String)
}
