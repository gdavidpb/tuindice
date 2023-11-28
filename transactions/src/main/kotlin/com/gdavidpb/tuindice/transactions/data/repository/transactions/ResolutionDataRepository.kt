package com.gdavidpb.tuindice.transactions.data.repository.transactions

import com.gdavidpb.tuindice.transactions.domain.repository.ResolutionRepository

class ResolutionDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource
) : ResolutionRepository {
	override suspend fun syncTransactions(uid: String) {
		val pendingTransactions = localDataSource.getTransactionsQueue(uid)

		remoteDataSource.sync(pendingTransactions)
	}
}