package com.gdavidpb.tuindice.persistence.data.api

import com.gdavidpb.tuindice.persistence.data.api.request.SyncTransactionRequest
import com.gdavidpb.tuindice.persistence.data.api.response.SyncResolutionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApi {
	@POST("sync")
	suspend fun sync(
		@Body request: List<SyncTransactionRequest<*>>
	): Response<List<SyncResolutionResponse<*>>>
}