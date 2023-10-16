package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EvaluationDao : UpsertDao<EvaluationEntity>() {
	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"ORDER BY ${EvaluationTable.DATE} ASC"
	)
	@Transaction
	abstract fun getEvaluationsWithSubject(
		uid: String
	): Flow<List<EvaluationWithSubject>>

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.ID} = :eid"
	)
	@Transaction
	abstract fun getEvaluationWithSubject(
		uid: String,
		eid: String
	): Flow<EvaluationWithSubject?>

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.SUBJECT_ID} = :sid " +
				"ORDER BY ${EvaluationTable.DATE} ASC"
	)
	abstract fun getSubjectEvaluations(
		uid: String,
		sid: String
	): Flow<List<EvaluationEntity>>

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
				"AND ${EvaluationTable.SUBJECT_ID} = :sid " +
				"AND ${EvaluationTable.TYPE} = :type " +
				"ORDER BY ${EvaluationTable.DATE} ASC"
	)
	abstract suspend fun getSubjectEvaluationsByType(
		uid: String,
		sid: String,
		type: Int
	): List<EvaluationEntity>

	@Query(
		"UPDATE ${EvaluationTable.TABLE_NAME} " +
				"SET ${EvaluationTable.ORDINAL} = :ordinal " +
				"WHERE ${EvaluationTable.ACCOUNT_ID} = :uid " +
				"AND ${EvaluationTable.ID} = :eid"
	)
	abstract suspend fun updateEvaluationOrdinalById(
		uid: String,
		eid: String,
		ordinal: Int
	)
}