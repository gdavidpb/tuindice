package com.gdavidpb.tuindice.persistence.data.transaction

import androidx.room.withTransaction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.tracker.source.TransactionDataSource
import com.gdavidpb.tuindice.persistence.data.transaction.mapper.toTransaction
import com.gdavidpb.tuindice.persistence.data.transaction.mapper.toTransactionEntity
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionAction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus

class RoomDataSource(
	private val room: TuIndiceDatabase
) : TransactionDataSource {
	override suspend fun getPendingTransactions(): List<Transaction> {
		return room.transactions.getTransactions(status = TransactionStatus.PENDING)
			.map { transactionEntity -> transactionEntity.toTransaction() }
	}

	override suspend fun createTransaction(transaction: Transaction) {
		if (transaction.action != TransactionAction.DELETE)
			internalCreateTransaction(transaction)
		else
			room.withTransaction {
				internalDiscardTransactions(transaction)
				internalCreateTransaction(transaction)
			}
	}

	override suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus) {
		room.transactions.updateTransactionStatus(transactionId = transactionId, status = status)
	}

	private suspend fun internalCreateTransaction(transaction: Transaction) {
		val transactionEntity = transaction.toTransactionEntity()

		room.transactions.createTransaction(entity = transactionEntity)
	}

	private suspend fun internalDiscardTransactions(transaction: Transaction) {
		room.transactions.deleteTransactionsByReference(reference = transaction.reference)
	}
}