package com.gdavidpb.tuindice.persistence.data.api

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.persistence.data.api.mapper.toSyncTransactionRequest
import com.gdavidpb.tuindice.persistence.data.api.mapper.toTransactionResult
import com.gdavidpb.tuindice.persistence.data.tracker.source.RemoteDataSource
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionData
import com.gdavidpb.tuindice.persistence.domain.model.TransactionResult

class ApiDataSource(
	private val syncApi: SyncApi
) : RemoteDataSource {
	override suspend fun sync(transactions: List<Transaction<TransactionData>>): List<TransactionResult> {
		val request = transactions.map { transaction -> transaction.toSyncTransactionRequest() }

		return syncApi.sync(request)
			.getOrThrow()
			.map { syncResolutionResponse -> syncResolutionResponse.toTransactionResult() }
	}
}