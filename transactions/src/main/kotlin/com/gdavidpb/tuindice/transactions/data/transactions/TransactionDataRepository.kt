package com.gdavidpb.tuindice.transactions.data.transactions

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.transactions.data.transactions.source.LocalDataSource
import com.gdavidpb.tuindice.transactions.data.transactions.source.SchedulerDataSource
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository

internal class TransactionDataRepository(
	private val localDataSource: LocalDataSource,
	private val schedulerDataSource: SchedulerDataSource
) : TransactionRepository {
	override suspend fun enqueueTransaction(transaction: Transaction<*>) {
		localDataSource.enqueueTransaction(transaction)

		schedulerDataSource.scheduleSync()
	}

	override suspend fun getTransactionsQueue(uid: String): List<Transaction<*>> {
		return localDataSource.getTransactionsQueue(uid)
	}
}