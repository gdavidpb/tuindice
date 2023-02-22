package com.gdavidpb.tuindice.persistence.data.tracker

import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.persistence.data.tracker.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.RemoteDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionAction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus
import com.gdavidpb.tuindice.persistence.domain.model.TransactionType
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository

class TrackerDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val schedulerDataSource: SchedulerDataSource
) : TrackerRepository {
	override suspend fun syncPendingTransactions() {
		runCatching {
			val pendingTransactions = localDataSource.getPendingTransactions()

			localDataSource.updateTransactionsStatus(
				from = TransactionStatus.PENDING,
				to = TransactionStatus.IN_PROGRESS
			)

			remoteDataSource.sync(transactions = pendingTransactions)
		}.onSuccess {
			localDataSource.updateTransactionsStatus(
				from = TransactionStatus.IN_PROGRESS,
				to = TransactionStatus.COMPLETED
			)
		}.onFailure { throwable ->
			localDataSource.updateTransactionsStatus(
				from = TransactionStatus.IN_PROGRESS,
				to = TransactionStatus.PENDING
			)

			throw throwable
		}
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

			localDataSource.createTransaction(transaction)

			runCatching {
				block()
			}.onSuccess {
				localDataSource.updateTransactionStatus(
					transactionId = transaction.id,
					status = TransactionStatus.COMPLETED
				)
			}.onFailure {
				// TODO pending only for communication failures

				localDataSource.updateTransactionStatus(
					transactionId = transaction.id,
					status = TransactionStatus.PENDING
				)

				schedulerDataSource.enqueueSync()
			}
		}
	}
}