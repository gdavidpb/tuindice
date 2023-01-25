package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.QuarterWithSubjects
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

@Dao
interface QuarterDao : BaseDao<QuarterEntity> {
	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.ID} = :qid"
	)
	@Transaction
	suspend fun getQuarter(uid: String, qid: String): QuarterWithSubjects

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.STATUS} = $STATUS_QUARTER_CURRENT"
	)
	@Transaction
	suspend fun getCurrentQuarter(uid: String): QuarterWithSubjects?

	@Query(
		"SELECT * FROM ${QuarterTable.TABLE_NAME} " +
				"JOIN ${SubjectTable.TABLE_NAME} " +
				"ON ${QuarterTable.TABLE_NAME}.${QuarterTable.ID} = ${SubjectTable.TABLE_NAME}.${SubjectTable.QUARTER_ID} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid"
	)
	suspend fun getQuarters(uid: String): Map<QuarterEntity, List<SubjectEntity>>

	@Query(
		"DELETE FROM ${QuarterTable.TABLE_NAME} " +
				"WHERE ${QuarterTable.ACCOUNT_ID} = :uid " +
				"AND ${QuarterTable.ID} = :qid"
	)
	suspend fun deleteQuarter(uid: String, qid: String): Int
}