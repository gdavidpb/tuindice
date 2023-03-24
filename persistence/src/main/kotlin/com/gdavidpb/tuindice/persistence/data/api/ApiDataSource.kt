package com.gdavidpb.tuindice.persistence.data.api

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.persistence.data.api.mapper.toResolution
import com.gdavidpb.tuindice.persistence.data.api.mapper.toTransactionRequest
import com.gdavidpb.tuindice.persistence.data.tracker.source.RemoteDataSource
import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution

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