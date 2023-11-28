package com.gdavidpb.tuindice.transactions.data.repository.transactions.source

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.transactions.data.repository.transactions.SyncApi
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.mapper.toResolution
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.mapper.toTransactionRequest
import com.gdavidpb.tuindice.transactions.data.repository.transactions.RemoteDataSource

class SyncApiDataSource(
	private val syncApi: SyncApi
) : RemoteDataSource {
	override suspend fun sync(transactions: List<Transaction>): List<Resolution> {
		val request = transactions.map { transaction -> transaction.toTransactionRequest() }

		return syncApi.sync(request)
			.getOrThrow()
			.map { resolutionResponse -> resolutionResponse.toResolution() }
	}
}