package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.TransactionTable
import java.util.*

@Dao
abstract class TransactionDao {
	@Query(
		"SELECT * FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.ACCOUNT_ID} = :uid " +
				"ORDER BY ${TransactionTable.ORDINAL} ASC"
	)
	abstract suspend fun getTransactions(
		uid: String
	): List<TransactionEntity>

	@Upsert
	abstract suspend fun upsertTransaction(
		entity: TransactionEntity
	)

	@Query(
		"DELETE FROM ${TransactionTable.TABLE_NAME} " +
				"WHERE ${TransactionTable.REFERENCE} = :reference"
	)
	abstract suspend fun deleteTransactionsByReference(
		reference: String
	)
}