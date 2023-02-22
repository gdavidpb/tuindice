package com.gdavidpb.tuindice.persistence.data.api

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.persistence.data.tracker.source.RemoteDataSource
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionResult

class ApiDataSource(
	private val syncApi: SyncApi
) : RemoteDataSource {
	override suspend fun sync(transactions: List<Transaction>): List<TransactionResult> {
		val request = transactions.map { transaction -> TODO("$transaction") }

		return syncApi.sync(request)
			.getOrThrow()
			.map { syncResponse -> TODO("$syncResponse") }
	}
}