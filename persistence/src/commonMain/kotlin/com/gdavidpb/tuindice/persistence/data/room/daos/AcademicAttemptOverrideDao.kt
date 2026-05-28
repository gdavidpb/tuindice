package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicAttemptOverrideTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicAttemptOverrideDao : UpsertDao<AcademicAttemptOverrideEntity>() {
	@Query("SELECT * FROM ${AcademicAttemptOverrideTable.TABLE_NAME} ORDER BY ${AcademicAttemptOverrideTable.UPDATED_AT} DESC")
	abstract fun observeOverridesFlow(): Flow<List<AcademicAttemptOverrideEntity>>

	@Query("SELECT * FROM ${AcademicAttemptOverrideTable.TABLE_NAME} ORDER BY ${AcademicAttemptOverrideTable.UPDATED_AT} DESC")
	abstract suspend fun getOverrides(): List<AcademicAttemptOverrideEntity>

	@Query("DELETE FROM ${AcademicAttemptOverrideTable.TABLE_NAME} WHERE ${AcademicAttemptOverrideTable.ATTEMPT_ID} = :attemptId")
	abstract suspend fun deleteByAttemptId(attemptId: String)

	@Query("DELETE FROM ${AcademicAttemptOverrideTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
