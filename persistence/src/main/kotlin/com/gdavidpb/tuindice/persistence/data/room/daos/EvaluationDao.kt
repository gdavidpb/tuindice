package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationTable

@Dao
abstract class EvaluationDao {
	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.ID} = :eid"
	)
	abstract suspend fun getEvaluation(
		uid: String,
		eid: String
	): EvaluationEntity

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	abstract suspend fun insertEvaluations(
		evaluations: List<EvaluationEntity>
	)

	@Query(
		"DELETE FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.ID} = :eid"
	)
	abstract suspend fun deleteEvaluation(
		uid: String,
		eid: String
	): Int

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.SUBJECT_ID} = :sid"
	)
	abstract suspend fun getSubjectEvaluations(
		uid: String,
		sid: String
	): List<EvaluationEntity>
}