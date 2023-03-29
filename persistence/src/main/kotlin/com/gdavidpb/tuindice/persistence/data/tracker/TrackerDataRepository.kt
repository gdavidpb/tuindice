package com.gdavidpb.tuindice.persistence.data.tracker

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.persistence.data.tracker.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.RemoteDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository

class TrackerDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val schedulerDataSource: SchedulerDataSource
) : TrackerRepository {
	override suspend fun syncPendingTransactions(uid: String) {
		val transactions = localDataSource.getTransactionsQueue(uid)

		val resolutions = remoteDataSource.sync(transactions)

		localDataSource.applyResolutions(resolutions)
	}

	override suspend fun trackTransaction(transaction: Transaction<*>, block: suspend () -> Unit) {
		noAwait {
			runCatching {
				block()
			}.onFailure {
				localDataSource.enqueueTransaction(transaction)

				schedulerDataSource.scheduleSync()
			}
		}
	}
}