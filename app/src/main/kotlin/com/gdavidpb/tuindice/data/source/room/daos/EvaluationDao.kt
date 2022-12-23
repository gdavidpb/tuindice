package com.gdavidpb.tuindice.data.source.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.data.source.room.entities.EvaluationEntity
import com.gdavidpb.tuindice.data.source.room.schema.EvaluationTable

@Dao
interface EvaluationDao : BaseDao<EvaluationEntity> {
	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.ID} = :eid"
	)
	suspend fun getEvaluation(uid: String, eid: String): EvaluationEntity

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.SUBJECT_ID} = :sid"
	)
	suspend fun getSubjectEvaluations(uid: String, sid: String): List<EvaluationEntity>

	@Query(
		"DELETE FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.ID} = :eid"
	)
	suspend fun deleteEvaluation(uid: String, eid: String): Int
}