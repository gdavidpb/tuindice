package com.gdavidpb.tuindice.data.repository.messaging

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface MessagingApi {
	@POST("messaging")
	suspend fun enroll(
		@Query("messagingToken") messagingToken: String
	): Response<Unit>
}