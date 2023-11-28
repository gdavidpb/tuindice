package com.gdavidpb.tuindice.transactions.data.repository.transactions.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.mapper.toTransaction
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.mapper.toTransactionEntity
import com.gdavidpb.tuindice.transactions.data.repository.transactions.LocalDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.resolution.ResolutionApplier

class RoomDataSource(
	private val room: TuIndiceDatabase,
	private val resolutionApplier: ResolutionApplier
) : LocalDataSource {
	override suspend fun getTransactionsQueue(uid: String): List<Transaction> {
		return room.transactions.getTransactions(uid)
			.map { transactionEntity -> transactionEntity.toTransaction() }
	}

	override suspend fun enqueueTransaction(uid: String, transaction: Transaction): String {
		return if (transaction.action != TransactionAction.DELETE)
			internalCreateTransaction(uid, transaction)
		else
			room.withTransaction {
				internalDiscardTransactions(uid, transaction)
				internalCreateTransaction(uid, transaction)
			}
	}

	override suspend fun applyResolutions(resolutions: List<Resolution>) {
		resolutionApplier.apply(resolutions)
	}

	private suspend fun internalCreateTransaction(uid: String, transaction: Transaction): String {
		val transactionOrdinal = room.counters.getAndIncrement(uid)
		val transactionEntity = transaction.toTransactionEntity(uid, transactionOrdinal)

		room.transactions.upsertTransaction(entity = transactionEntity)

		return transactionEntity.id
	}

	private suspend fun internalDiscardTransactions(uid: String, transaction: Transaction) {
		room.transactions.deleteTransactionsByReference(
			uid = uid,
			reference = transaction.reference
		)
	}
}