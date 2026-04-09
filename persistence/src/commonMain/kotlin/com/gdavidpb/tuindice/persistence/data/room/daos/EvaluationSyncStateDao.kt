package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationSyncStateEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationSyncStateTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EvaluationSyncStateDao : UpsertDao<EvaluationSyncStateEntity>() {
	@Query(
		"SELECT * FROM ${EvaluationSyncStateTable.TABLE_NAME} " +
			"WHERE ${EvaluationSyncStateTable.KEY} = :key " +
			"LIMIT 1"
	)
	abstract fun observeSyncState(key: String = EvaluationSyncStateTable.DEFAULT_KEY): Flow<EvaluationSyncStateEntity?>

	@Query(
		"SELECT * FROM ${EvaluationSyncStateTable.TABLE_NAME} " +
			"WHERE ${EvaluationSyncStateTable.KEY} = :key " +
			"LIMIT 1"
	)
	abstract suspend fun getSyncState(key: String = EvaluationSyncStateTable.DEFAULT_KEY): EvaluationSyncStateEntity?

	@Query("DELETE FROM ${EvaluationSyncStateTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
