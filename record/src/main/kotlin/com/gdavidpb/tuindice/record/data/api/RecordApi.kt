package com.gdavidpb.tuindice.record.data.api

import com.gdavidpb.tuindice.record.data.api.request.RemoveQuarterRequest
import com.gdavidpb.tuindice.record.data.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.api.response.QuarterResponse
import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse
import com.gdavidpb.tuindice.transactions.domain.annotation.EnqueueOnFailure
import retrofit2.Response
import retrofit2.http.*

interface RecordApi {
	@GET("quarters")
	suspend fun getQuarters(): Response<List<QuarterResponse>>

	@EnqueueOnFailure
	@DELETE("quarters")
	suspend fun deleteQuarter(
		@Body request: RemoveQuarterRequest
	): Response<Unit>

	@EnqueueOnFailure
	@PATCH("subjects")
	suspend fun updateSubject(
		@Body request: UpdateSubjectRequest
	): Response<SubjectResponse>
}