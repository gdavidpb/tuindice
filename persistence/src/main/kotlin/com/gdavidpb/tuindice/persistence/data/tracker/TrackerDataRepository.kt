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

			val resolutions = remoteDataSource.sync(transactions = pendingTransactions)

			localDataSource.applyResolutions(resolutions)
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

	override suspend fun trackTransaction(transaction: Transaction, block: suspend () -> Unit) {
		noAwait {
			localDataSource.createTransaction(transaction)

			runCatching {
				block()
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