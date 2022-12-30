package com.gdavidpb.tuindice.summary.data.source.api

import com.gdavidpb.tuindice.summary.data.source.api.responses.ProfilePictureResponse
import retrofit2.Response
import retrofit2.http.*

interface SummaryAPI {
	@GET("profilePicture")
	suspend fun getProfilePicture(
		@Query("uid") uid: String
	): Response<ProfilePictureResponse>

	@POST("profilePicture")
	suspend fun uploadProfilePicture(
		@Body encodedPicture: String
	): Response<ProfilePictureResponse>

	@DELETE("profilePicture")
	suspend fun deleteProfilePicture(): Response<Unit>
}