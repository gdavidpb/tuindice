package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicSummaryEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicSummaryTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicSummaryDao : UpsertDao<AcademicSummaryEntity>() {
	@Query("SELECT * FROM ${AcademicSummaryTable.TABLE_NAME} LIMIT 1")
	abstract fun observeSummaryFlow(): Flow<AcademicSummaryEntity?>

	@Query("SELECT * FROM ${AcademicSummaryTable.TABLE_NAME} LIMIT 1")
	abstract suspend fun getSummary(): AcademicSummaryEntity?

	@Query("DELETE FROM ${AcademicSummaryTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
