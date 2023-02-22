package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.TransactionTable
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus
import java.util.*

@Dao
abstract class TransactionDao {
	@Query(
		"SELECT * FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.STATUS} = :status " +
				"ORDER BY ${TransactionTable.TIMESTAMP} ASC"
	)
	abstract suspend fun getTransactions(
		status: TransactionStatus
	): List<TransactionEntity>

	@Upsert
	abstract suspend fun createTransaction(
		entity: TransactionEntity
	)

	@Query(
		"DELETE FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.REFERENCE} = :reference"
	)
	abstract suspend fun deleteTransactionsByReference(
		reference: String
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
		"UPDATE ${TransactionTable.TABLE_NAME} " +
				"SET ${TransactionTable.STATUS} = :to " +
				"WHERE ${TransactionTable.STATUS} = :from"
	)
	abstract suspend fun updateTransactionsStatus(
		from: TransactionStatus,
		to: TransactionStatus
	)

	@Query(
		"DELETE FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.STATUS} = :status"
	)
	abstract suspend fun prune(
		status: TransactionStatus
	)
}