package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.data.source.functions.requests.CheckCredentialsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TuIndiceAPI {
	@POST("checkCredentials")
	suspend fun checkCredentials(
		@Body request: CheckCredentialsRequest
	): Response<Unit>
}