package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.TransactionTable

@Dao
abstract class TransactionDao {
	@Query(
		"SELECT * FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.ACCOUNT_ID} = :uid " +
				"ORDER BY ${TransactionTable.TIMESTAMP} ASC"
	)
	abstract suspend fun getTransactions(
		uid: String
	): List<TransactionEntity>

	@Upsert
	abstract suspend fun enqueueTransaction(
		entity: TransactionEntity
	)

	@Query(
		"DELETE FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.ACCOUNT_ID} = :uid " +
				"AND ${TransactionTable.ID} = :tid"
	)
	abstract suspend fun dequeueTransaction(
		uid: String,
		tid: String
	)

	@Query(
		"DELETE FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.ACCOUNT_ID} = :uid " +
				"AND ${TransactionTable.REFERENCE} = :reference"
	)
	abstract suspend fun dequeueTransactionsByReference(
		uid: String,
		reference: String
	)
}