package com.gdavidpb.tuindice.persistence.data.api

import com.gdavidpb.tuindice.persistence.data.api.request.TransactionRequest
import com.gdavidpb.tuindice.persistence.data.api.response.ResolutionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApi {
	@POST("sync")
	suspend fun sync(
		@Body request: List<TransactionRequest>
	): Response<List<ResolutionResponse>>
}