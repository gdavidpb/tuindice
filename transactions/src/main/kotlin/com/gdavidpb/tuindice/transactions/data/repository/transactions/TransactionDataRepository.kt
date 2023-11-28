package com.gdavidpb.tuindice.transactions.data.repository.transactions

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository

class TransactionDataRepository(
	private val localDataSource: LocalDataSource,
	private val schedulerDataSource: SchedulerDataSource
) : TransactionRepository {
	override suspend fun enqueueTransaction(uid: String, transaction: Transaction) {
		localDataSource.enqueueTransaction(uid, transaction)

		schedulerDataSource.scheduleSync()
	}
}