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
				"ORDER BY ${EvaluationTable.DATE} ASC"
	)
	@Transaction
	abstract fun getEvaluationsWithSubjectFlow(): Flow<List<EvaluationWithSubject>>

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ID} = :eid"
	)
	@Transaction
	abstract suspend fun getEvaluationWithSubject(
		eid: String
	): EvaluationWithSubject?

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ID} = :eid"
	)
	abstract suspend fun getEvaluation(
		eid: String
	): EvaluationEntity

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.SUBJECT_ID} = :sid " +
				"ORDER BY ${EvaluationTable.DATE} ASC"
	)
	abstract fun getSubjectEvaluations(
		sid: String
	): Flow<List<EvaluationEntity>>

	@Query(
		"DELETE FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ID} = :eid"
	)
	abstract suspend fun deleteEvaluation(
		eid: String
	): Int
}
