package com.gdavidpb.tuindice.persistence.data.room.internal

import androidx.room.withTransaction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.internal.mapper.toTransaction
import com.gdavidpb.tuindice.persistence.data.room.internal.mapper.toTransactionEntity
import com.gdavidpb.tuindice.persistence.data.tracker.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.domain.model.Resolution
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionAction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus

internal class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun applyResolutions(resolutions: List<Resolution>) {
		TODO("Not yet implemented")
	}

	override suspend fun getPendingTransactions(): List<Transaction> {
		return room.transactions.getTransactions(status = TransactionStatus.PENDING)
			.map { transactionEntity -> transactionEntity.toTransaction() }
	}

	override suspend fun createTransaction(transaction: Transaction): String {
		return if (transaction.action != TransactionAction.DELETE)
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

	override suspend fun updateTransactionsStatus(from: TransactionStatus, to: TransactionStatus) {
		room.transactions.updateTransactionsStatus(from = from, to = to)
	}

	private suspend fun internalCreateTransaction(transaction: Transaction): String {
		val transactionEntity = transaction.toTransactionEntity()

		room.transactions.createTransaction(entity = transactionEntity)

		return transactionEntity.id
	}

	private suspend fun internalDiscardTransactions(transaction: Transaction) {
		room.transactions.deleteTransactionsByReference(reference = transaction.reference)
	}
}