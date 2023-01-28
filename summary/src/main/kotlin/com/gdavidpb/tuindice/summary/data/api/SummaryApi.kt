package com.gdavidpb.tuindice.summary.data.api

import com.gdavidpb.tuindice.summary.data.api.response.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.data.api.response.AccountResponse
import retrofit2.Response
import retrofit2.http.*

interface SummaryApi {
	@GET("accounts")
	suspend fun getAccount(): Response<AccountResponse>

	@POST("accounts/picture")
	suspend fun uploadProfilePicture(
		@Body encodedPicture: String
	): Response<ProfilePictureResponse>

	@DELETE("accounts/picture")
	suspend fun deleteProfilePicture(): Response<Unit>
}