package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.data.source.functions.requests.AddQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.requests.UpdateQuarterRequest
import com.gdavidpb.tuindice.data.source.functions.responses.*
import retrofit2.Response
import retrofit2.http.*

interface TuIndiceAPI {
	/* Session */

	@POST("signIn")
	suspend fun signIn(
		@Header("Authorization") basicToken: String,
		@Header("Re-Authenticate") refreshToken: Boolean
	): Response<SignInResponse>

	/* Quarters */

	@GET("quarters")
	suspend fun getQuarters(): Response<List<QuarterResponse>>

	@POST("quarters")
	suspend fun addQuarter(
		@Body request: AddQuarterRequest
	): Response<AddQuarterResponse>

	@PATCH("quarters")
	suspend fun updateQuarter(
		@Body request: UpdateQuarterRequest
	): Response<UpdateQuarterResponse>

	@DELETE("quarters")
	suspend fun deleteQuarter(
		@Query("qid") quarterId: String
	): Response<Unit>

	/* Enrollment */

	@GET("enrollmentProof")
	suspend fun enrollmentProof(): Response<EnrollmentProofResponse>
}