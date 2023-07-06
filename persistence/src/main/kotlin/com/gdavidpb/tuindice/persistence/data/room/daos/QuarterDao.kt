package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.*
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.QuarterWithSubjects
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuarterDao : UpsertDao<QuarterEntity>() {
	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.ID} = :qid"
	)
	@Transaction
	abstract suspend fun getQuarterWithSubjects(
		uid: String,
		qid: String
	): QuarterWithSubjects

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.STATUS} = $STATUS_QUARTER_CURRENT"
	)
	abstract fun getCurrentQuarterWithSubjects(
		uid: String
	): Flow<QuarterWithSubjects?>

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid"
	)
	@Transaction
	abstract fun getQuartersWithSubjects(
		uid: String
	): Flow<List<QuarterWithSubjects>>

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.STATUS} = $STATUS_QUARTER_CURRENT"
	)
	abstract suspend fun getCurrentQuarter(
		uid: String
	): QuarterEntity?

	@Query(
		"DELETE FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.ID} = :qid"
	)
	abstract suspend fun deleteQuarter(
		uid: String,
		qid: String
	): Int

	@Query(
		"UPDATE ${QuarterTable.TABLE_NAME} " +
				"SET ${QuarterTable.ID} = :toId " +
				"WHERE ${QuarterTable.ID} = :fromId"
	)
	abstract suspend fun updateId(
		fromId: String,
		toId: String
	)
}