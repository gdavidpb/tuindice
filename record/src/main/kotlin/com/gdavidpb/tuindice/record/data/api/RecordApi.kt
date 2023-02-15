package com.gdavidpb.tuindice.record.data.api

import com.gdavidpb.tuindice.record.data.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.api.response.QuarterResponse
import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse
import retrofit2.Response
import retrofit2.http.*

interface RecordApi {
	@GET("quarters")
	suspend fun getQuarters(): Response<List<QuarterResponse>>

	@DELETE("quarters")
	suspend fun deleteQuarter(
		@Query("qid") quarterId: String
	): Response<Unit>

	@PATCH("subjects")
	suspend fun updateSubject(
		@Body request: UpdateSubjectRequest
	): Response<SubjectResponse>
}