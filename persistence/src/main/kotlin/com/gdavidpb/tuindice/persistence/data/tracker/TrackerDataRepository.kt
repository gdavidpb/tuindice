package com.gdavidpb.tuindice.persistence.data.tracker

import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.persistence.data.tracker.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.RemoteDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.domain.model.*
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

			// TODO process server response
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
		data: TransactionData?,
		remote: suspend () -> Unit
	) {
		noAwait {
			val transaction = Transaction(
				reference = reference,
				type = type,
				action = action,
				status = TransactionStatus.IN_PROGRESS,
				timestamp = System.currentTimeMillis(),
				data = data
			)

			localDataSource.createTransaction(transaction)

			runCatching {
				remote()
			}.onSuccess {
				localDataSource.updateTransactionStatus(
					transactionId = transaction.id,
					status = TransactionStatus.COMPLETED
				)
			}.onFailure {
				localDataSource.updateTransactionStatus(
					transactionId = transaction.id,
					status = TransactionStatus.PENDING
				)

				schedulerDataSource.enqueueSync()
			}
		}
	}
}