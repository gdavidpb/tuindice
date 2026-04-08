package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicRecordTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicRecordDao : UpsertDao<AcademicRecordEntity>() {
	@Query("SELECT * FROM ${AcademicRecordTable.TABLE_NAME} LIMIT 1")
	abstract fun observeRecordFlow(): Flow<AcademicRecordEntity?>

	@Query("SELECT * FROM ${AcademicRecordTable.TABLE_NAME} LIMIT 1")
	abstract suspend fun getRecord(): AcademicRecordEntity?

	@Query("DELETE FROM ${AcademicRecordTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
