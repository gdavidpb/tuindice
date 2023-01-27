package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.*
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.QuarterWithSubjects
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

@Dao
interface QuarterDao {
	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.ID} = :qid"
	)
	@Transaction
	suspend fun getQuarterWithSubjects(
		uid: String,
		qid: String
	): QuarterWithSubjects

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"JOIN ${SubjectTable.TABLE_NAME} " +
				"ON ${QuarterTable.TABLE_NAME}.${QuarterTable.ID} = ${SubjectTable.TABLE_NAME}.${SubjectTable.QUARTER_ID} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid"
	)
	suspend fun getQuartersWithSubjects(
		uid: String
	): Map<QuarterEntity, List<SubjectEntity>>

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.STATUS} = $STATUS_QUARTER_CURRENT"
	)
	@Transaction
	suspend fun getCurrentQuarterWithSubject(
		uid: String
	): QuarterWithSubjects?

	@Query(
		"DELETE FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.ID} = :qid"
	)
	suspend fun deleteQuarter(
		uid: String,
		qid: String
	): Int

	@Transaction
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertQuartersAndSubjects(
		quarters: List<QuarterEntity>,
		subjects: List<SubjectEntity>
	)
}