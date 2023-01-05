package com.gdavidpb.tuindice.summary.data.api

import com.gdavidpb.tuindice.summary.data.api.responses.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.data.api.responses.AccountResponse
import retrofit2.Response
import retrofit2.http.*

interface SummaryApi {
	@GET("account")
	suspend fun getAccount(): Response<AccountResponse>

	@POST("account/picture")
	suspend fun uploadProfilePicture(
		@Body encodedPicture: String
	): Response<ProfilePictureResponse>

	@DELETE("account/picture")
	suspend fun deleteProfilePicture(): Response<Unit>
}