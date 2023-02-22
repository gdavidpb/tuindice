package com.gdavidpb.tuindice.persistence.data.transaction

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.tracker.source.TransactionDataSource
import com.gdavidpb.tuindice.persistence.data.transaction.mapper.toTransaction
import com.gdavidpb.tuindice.persistence.data.transaction.mapper.toTransactionEntity
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus

class RoomDataSource(
	private val room: TuIndiceDatabase
) : TransactionDataSource {
	override suspend fun getPendingTransactions(): List<Transaction> {
		return room.transactions.getTransactions(TransactionStatus.PENDING)
			.map { transactionEntity -> transactionEntity.toTransaction() }
	}

	override suspend fun createTransaction(transaction: Transaction) {
		val transactionEntity = transaction.toTransactionEntity()

		room.transactions.createTransaction(transactionEntity)
	}

	override suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus) {
		room.transactions.updateTransactionStatus(transactionId, status)
	}
}