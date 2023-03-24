package com.gdavidpb.tuindice.persistence.data.room.internal

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.internal.mapper.toTransaction
import com.gdavidpb.tuindice.persistence.data.room.internal.mapper.toTransactionEntity
import com.gdavidpb.tuindice.persistence.data.tracker.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.domain.mapper.isSubject
import com.gdavidpb.tuindice.persistence.domain.mapper.toSubjectEntity

internal class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun applyResolutions(resolutions: List<Resolution>) {
		room.withTransaction {
			resolutions.forEach { resolution ->
				when {
					resolution.isSubject() -> handleSubjectResolution(resolution)
				}
			}
		}
	}

	override suspend fun getTransactionsQueue(): List<Transaction<*>> {
		return room.transactions.getTransactions()
			.map { transactionEntity -> transactionEntity.toTransaction() }
	}

	override suspend fun enqueueTransaction(transaction: Transaction<*>): String {
		return if (transaction.action != TransactionAction.DELETE)
			internalCreateTransaction(transaction)
		else
			room.withTransaction {
				internalDiscardTransactions(transaction)
				internalCreateTransaction(transaction)
			}
	}

	private suspend fun internalCreateTransaction(transaction: Transaction<*>): String {
		val transactionEntity = transaction.toTransactionEntity()

		room.transactions.upsertTransaction(entity = transactionEntity)

		return transactionEntity.id
	}

	private suspend fun internalDiscardTransactions(transaction: Transaction<*>) {
		room.transactions.deleteTransactionsByReference(reference = transaction.reference)
	}

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