package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser.QuarterRemoveParser
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser.SubjectUpdateParser
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.SubjectResponse
import com.gdavidpb.tuindice.transactions.domain.annotation.EnqueuedApi
import retrofit2.Response
import retrofit2.http.*

interface RecordApi {
	@GET("quarters")
	suspend fun getQuarters(): Response<List<QuarterResponse>>

	@EnqueuedApi(QuarterRemoveParser::class)
	@DELETE("quarters")
	suspend fun deleteQuarter(
		@Query("qid") quarterId: String
	): Response<Unit>

	@EnqueuedApi(SubjectUpdateParser::class)
	@PATCH("subjects")
	suspend fun updateSubject(
		@Body request: UpdateSubjectRequest
	): Response<SubjectResponse>
}