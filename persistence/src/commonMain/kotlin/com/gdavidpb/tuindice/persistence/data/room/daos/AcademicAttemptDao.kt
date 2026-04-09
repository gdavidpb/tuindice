package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicAttemptTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicAttemptDao : UpsertDao<AcademicAttemptEntity>() {
	@Query("SELECT * FROM ${AcademicAttemptTable.TABLE_NAME} ORDER BY ${AcademicAttemptTable.TERM_ID} DESC, ${AcademicAttemptTable.POSITION_IN_TERM} ASC, ${AcademicAttemptTable.ID} ASC")
	abstract fun observeAttemptsFlow(): Flow<List<AcademicAttemptEntity>>

	@Query("SELECT * FROM ${AcademicAttemptTable.TABLE_NAME} ORDER BY ${AcademicAttemptTable.TERM_ID} DESC, ${AcademicAttemptTable.POSITION_IN_TERM} ASC, ${AcademicAttemptTable.ID} ASC")
	abstract suspend fun getAttempts(): List<AcademicAttemptEntity>

	@Query("DELETE FROM ${AcademicAttemptTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
