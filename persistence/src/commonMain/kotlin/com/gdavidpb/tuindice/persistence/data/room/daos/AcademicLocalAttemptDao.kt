package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicLocalAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicLocalAttemptTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicLocalAttemptDao : UpsertDao<AcademicLocalAttemptEntity>() {
	@Query("SELECT * FROM ${AcademicLocalAttemptTable.TABLE_NAME} WHERE ${AcademicLocalAttemptTable.RECORD_ID} = :recordId ORDER BY ${AcademicLocalAttemptTable.TERM_ID} DESC, ${AcademicLocalAttemptTable.SEQUENCE_IN_TERM} ASC, ${AcademicLocalAttemptTable.ID} ASC")
	abstract fun observeAttemptsFlow(recordId: String): Flow<List<AcademicLocalAttemptEntity>>

	@Query("SELECT * FROM ${AcademicLocalAttemptTable.TABLE_NAME} WHERE ${AcademicLocalAttemptTable.RECORD_ID} = :recordId ORDER BY ${AcademicLocalAttemptTable.TERM_ID} DESC, ${AcademicLocalAttemptTable.SEQUENCE_IN_TERM} ASC, ${AcademicLocalAttemptTable.ID} ASC")
	abstract suspend fun getAttempts(recordId: String): List<AcademicLocalAttemptEntity>

	@Query("DELETE FROM ${AcademicLocalAttemptTable.TABLE_NAME} WHERE ${AcademicLocalAttemptTable.RECORD_ID} = :recordId")
	abstract suspend fun deleteByRecord(recordId: String)

	@Query("DELETE FROM ${AcademicLocalAttemptTable.TABLE_NAME} WHERE ${AcademicLocalAttemptTable.TERM_ID} = :termId")
	abstract suspend fun deleteByTermId(termId: String)
}
