package com.gdavidpb.tuindice.transactions.data.repository.transactions.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.transactions.domain.model.Resolution
import com.gdavidpb.tuindice.transactions.domain.model.ResolutionType
import com.gdavidpb.tuindice.transactions.domain.model.Transaction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.transactions.data.repository.transactions.LocalDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.mapper.toTransaction
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.mapper.toTransactionEntity
import com.gdavidpb.tuindice.transactions.domain.model.ResolutionHandler

class RoomDataSource(
	private val room: TuIndiceDatabase,
	private val handlers: Map<ResolutionType, ResolutionHandler>
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

	override suspend fun applyResolutions(uid: String, resolutions: List<Resolution>) {
		room.withTransaction {
			resolutions.forEach { resolution ->
				val resolutionHandler = handlers[resolution.type]

				if (resolutionHandler != null) {
					resolutionHandler.apply(resolution)

					room.transactions.deleteTransactionsByReference(
						uid = uid,
						reference = resolution.localReference
					)
				}
			}
		}
	}

	private suspend fun internalCreateTransaction(uid: String, transaction: Transaction): String {
		val transactionEntity = transaction.toTransactionEntity(uid)

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