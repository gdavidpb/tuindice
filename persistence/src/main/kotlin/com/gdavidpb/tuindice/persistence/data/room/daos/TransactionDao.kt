package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionStatus
import com.gdavidpb.tuindice.persistence.data.room.schema.TransactionTable
import java.util.*

@Dao
abstract class TransactionDao {
	@Query(
		"SELECT * FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.STATUS} = :status " +
				"ORDER BY ${TransactionTable.TIMESTAMP} ASC"
	)
	abstract suspend fun getQueue(
		status: TransactionStatus
	): List<TransactionEntity>

	@Upsert
	abstract suspend fun createTransaction(
		transactionEntity: TransactionEntity
	)

	@Query(
		"UPDATE ${TransactionTable.TABLE_NAME} " +
				"SET ${TransactionTable.STATUS} = :status " +
				"WHERE ${TransactionTable.ID} = :transactionId"
	)
	abstract suspend fun updateTransactionStatus(
		transactionId: String,
		status: TransactionStatus
	)

	@Query(
		"DELETE FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.STATUS} = :status"
	)
	abstract suspend fun prune(
		status: TransactionStatus
	)
}