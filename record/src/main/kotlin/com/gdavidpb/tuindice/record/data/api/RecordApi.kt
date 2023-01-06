package com.gdavidpb.tuindice.record.data.api

import com.gdavidpb.tuindice.record.data.api.responses.EnrollmentProofResponse
import com.gdavidpb.tuindice.record.data.api.responses.QuarterResponse
import com.gdavidpb.tuindice.record.data.api.responses.UpdateQuarterResponse
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest
import retrofit2.Response
import retrofit2.http.*

interface RecordApi {
	@GET("quarters")
	suspend fun getQuarters(): Response<List<QuarterResponse>>

	@DELETE("quarters")
	suspend fun deleteQuarter(
		@Query("qid") quarterId: String
	): Response<Unit>

	@PATCH("quarters")
	suspend fun updateQuarter(
		@Body request: UpdateQuarterRequest
	): Response<UpdateQuarterResponse>

	@GET("enrollmentProof")
	suspend fun enrollmentProof(): Response<EnrollmentProofResponse>
}