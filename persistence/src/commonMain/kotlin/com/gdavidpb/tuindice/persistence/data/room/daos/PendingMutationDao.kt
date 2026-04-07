package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.PendingMutationTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PendingMutationDao : UpsertDao<PendingMutationEntity>() {
	@Query(
		"SELECT * FROM ${PendingMutationTable.TABLE_NAME} " +
			"WHERE ${PendingMutationTable.STORE_ID} = :storeId " +
			"AND ${PendingMutationTable.SCOPE_KEY} = :scopeKey " +
			"AND ${PendingMutationTable.STATUS} = 'Pending' " +
			"ORDER BY ${PendingMutationTable.CREATED_AT} ASC"
	)
	abstract fun observePendingMutations(storeId: String, scopeKey: String): Flow<List<PendingMutationEntity>>

	@Query(
		"SELECT * FROM ${PendingMutationTable.TABLE_NAME} " +
			"WHERE ${PendingMutationTable.STORE_ID} = :storeId " +
			"AND ${PendingMutationTable.SCOPE_KEY} = :scopeKey " +
			"AND ${PendingMutationTable.STATUS} = 'Pending' " +
			"ORDER BY ${PendingMutationTable.CREATED_AT} ASC"
	)
	abstract suspend fun getPendingMutations(storeId: String, scopeKey: String): List<PendingMutationEntity>

	@Query(
		"SELECT * FROM ${PendingMutationTable.TABLE_NAME} " +
			"WHERE ${PendingMutationTable.STORE_ID} = :storeId " +
			"AND ${PendingMutationTable.SCOPE_KEY} = :scopeKey " +
			"AND ${PendingMutationTable.MUTATION_ID} = :mutationId " +
			"LIMIT 1"
	)
	abstract suspend fun getPendingMutation(storeId: String, scopeKey: String, mutationId: String): PendingMutationEntity?

	@Query(
		"SELECT * FROM ${PendingMutationTable.TABLE_NAME} " +
			"WHERE ${PendingMutationTable.MUTATION_ID} = :mutationId " +
			"LIMIT 1"
	)
	abstract suspend fun getPendingMutation(mutationId: String): PendingMutationEntity?

	@Query(
		"DELETE FROM ${PendingMutationTable.TABLE_NAME} " +
			"WHERE ${PendingMutationTable.STORE_ID} = :storeId " +
			"AND ${PendingMutationTable.SCOPE_KEY} = :scopeKey " +
			"AND ${PendingMutationTable.MUTATION_ID} = :mutationId"
	)
	abstract suspend fun deletePendingMutation(storeId: String, scopeKey: String, mutationId: String): Int

	@Query(
		"DELETE FROM ${PendingMutationTable.TABLE_NAME} " +
			"WHERE ${PendingMutationTable.STORE_ID} = :storeId " +
			"AND ${PendingMutationTable.REPLACE_KEY} = :replaceKey"
	)
	abstract suspend fun deletePendingMutationsByReplaceKey(storeId: String, replaceKey: String): Int
}
