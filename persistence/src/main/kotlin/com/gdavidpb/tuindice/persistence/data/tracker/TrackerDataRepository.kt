package com.gdavidpb.tuindice.persistence.data.tracker

import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.TransactionDataSource
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionAction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus
import com.gdavidpb.tuindice.persistence.domain.model.TransactionType
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository

class TrackerDataRepository(
	private val schedulerDataSource: SchedulerDataSource,
	private val transactionDataSource: TransactionDataSource
) : TrackerRepository {
	override suspend fun getPendingTransactions(): List<Transaction> {
		return transactionDataSource.getPendingTransactions()
	}

	override suspend fun trackTransaction(
		reference: String,
		type: TransactionType,
		action: TransactionAction,
		block: suspend () -> Unit
	) {
		noAwait {
			val transaction = Transaction(
				reference = reference,
				type = type,
				action = action,
				status = TransactionStatus.IN_PROGRESS,
				timestamp = System.currentTimeMillis()
			)

			transactionDataSource.createTransaction(transaction)

			runCatching {
				block()
			}.onSuccess {
				transactionDataSource.updateTransactionStatus(
					transactionId = transaction.id,
					status = TransactionStatus.COMPLETED
				)
			}.onFailure {
				transactionDataSource.updateTransactionStatus(
					transactionId = transaction.id,
					status = TransactionStatus.PENDING
				)

				schedulerDataSource.enqueueSync()
			}
		}
	}
}