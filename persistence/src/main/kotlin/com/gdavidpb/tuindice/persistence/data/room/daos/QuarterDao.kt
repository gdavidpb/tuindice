package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.QuarterWithSubjects
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuarterDao : UpsertDao<QuarterEntity>() {
	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.IS_READ_ONLY} = 0"
	)
	@Transaction
	abstract fun getOpenQuartersWithSubjects(): List<QuarterWithSubjects>

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"ORDER BY ${QuarterTable.START_DATE} DESC"
	)
	@Transaction
	abstract fun getQuartersWithSubjectsFlow(): Flow<List<QuarterWithSubjects>>

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.IS_CURRENT} = 1"
	)
	abstract suspend fun getCurrentQuarter(): QuarterEntity?

	@Query(
		"DELETE FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ID} = :qid"
	)
	abstract suspend fun deleteQuarter(
		qid: String
	): Int
}