package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EvaluationDao : UpsertDao<EvaluationEntity>() {
	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"ORDER BY ${EvaluationTable.DATE} ASC"
	)
	abstract fun observeEvaluationsFlow(): Flow<List<EvaluationEntity>>

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ID} = :eid"
	)
	abstract suspend fun getEvaluation(eid: String): EvaluationEntity?

	@Query(
		"SELECT * FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ATTEMPT_ID} = :attemptId " +
				"ORDER BY ${EvaluationTable.DATE} ASC"
	)
	abstract fun getAttemptEvaluations(attemptId: String): Flow<List<EvaluationEntity>>

	@Query(
		"DELETE FROM ${EvaluationTable.TABLE_NAME} " +
				"WHERE ${EvaluationTable.ID} = :eid"
	)
	abstract suspend fun deleteEvaluation(eid: String): Int

	@Query("DELETE FROM ${EvaluationTable.TABLE_NAME}")
	abstract suspend fun deleteAll(): Int
}
