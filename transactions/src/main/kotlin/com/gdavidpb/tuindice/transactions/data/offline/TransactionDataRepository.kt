package com.gdavidpb.tuindice.transactions.data.offline

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.transactions.data.offline.source.LocalDataSource
import com.gdavidpb.tuindice.transactions.data.offline.source.SchedulerDataSource
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository

internal class TransactionDataRepository(
	private val localDataSource: LocalDataSource,
	private val schedulerDataSource: SchedulerDataSource
) : TransactionRepository {
	override suspend fun enqueueTransaction(transaction: Transaction<*>) {
		localDataSource.enqueueTransaction(transaction)

		schedulerDataSource.scheduleSync()
	}
}