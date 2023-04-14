package com.gdavidpb.tuindice.transactions.data.room

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.transactions.data.room.mapper.toTransaction
import com.gdavidpb.tuindice.transactions.data.room.mapper.toTransactionEntity
import com.gdavidpb.tuindice.transactions.data.offline.source.LocalDataSource
import com.gdavidpb.tuindice.transactions.domain.mapper.isSubject
import com.gdavidpb.tuindice.transactions.domain.mapper.toSubjectEntity

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getTransactionsQueue(uid: String): List<Transaction> {
		return room.transactions.getTransactions(uid)
			.map { transactionEntity -> transactionEntity.toTransaction() }
	}

	override suspend fun applyResolutions(uid: String, resolutions: List<Resolution>) {
		room.withTransaction {
			resolutions.forEach { resolution ->
				when {
					resolution.isSubject() -> handleSubjectResolution(resolution)
				}
			}
		}
	}

	// TODO additional queue logic (UPDATE, ADD and DELETE interactions)
	override suspend fun enqueueTransaction(uid: String, transaction: Transaction): String {
		return if (transaction.action != TransactionAction.DELETE)
			internalCreateTransaction(uid, transaction)
		else
			room.withTransaction {
				internalDiscardTransactions(transaction)
				internalCreateTransaction(uid, transaction)
			}
	}

	private suspend fun internalCreateTransaction(uid: String, transaction: Transaction): String {
		val transactionOrdinal = room.counters.getAndIncrement(uid)
		val transactionEntity = transaction.toTransactionEntity(uid, transactionOrdinal)

		room.transactions.upsertTransaction(entity = transactionEntity)

		return transactionEntity.id
	}

	// TODO include uid to this operation
	private suspend fun internalDiscardTransactions(transaction: Transaction) {
		room.transactions.deleteTransactionsByReference(reference = transaction.reference)
	}

	// TODO will there be a better way to do this?
	private suspend fun handleSubjectResolution(resolution: Resolution) {
		val subjectEntity = resolution.toSubjectEntity()

		if (resolution.localReference != resolution.remoteReference)
			room.subjects.updateId(
				fromId = resolution.localReference,
				toId = resolution.remoteReference
			)

		when (resolution.action) {
			ResolutionAction.ADD, ResolutionAction.UPDATE ->
				room.subjects.upsertEntities(
					entities = listOf(subjectEntity)
				)
			ResolutionAction.DELETE ->
				room.subjects.deleteSubject(
					uid = subjectEntity.accountId,
					sid = subjectEntity.id
				)
		}
	}
}