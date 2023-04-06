package com.gdavidpb.tuindice.transactions.data.api

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.transactions.data.api.mapper.toResolution
import com.gdavidpb.tuindice.transactions.data.api.mapper.toTransactionRequest
import com.gdavidpb.tuindice.transactions.data.transactions.source.RemoteDataSource

class ApiDataSource(
	private val syncApi: SyncApi
) : RemoteDataSource {
	override suspend fun sync(transactions: List<Transaction<*>>): List<Resolution> {
		val request = transactions.map { transaction -> transaction.toTransactionRequest() }

		return syncApi.sync(request)
			.getOrThrow()
			.map { resolutionResponse -> resolutionResponse.toResolution() }
	}
}