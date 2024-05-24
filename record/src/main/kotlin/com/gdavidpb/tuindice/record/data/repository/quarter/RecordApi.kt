package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.AddQuarterRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface RecordApi {
	@GET("quarters")
	suspend fun getQuarters(): Response<List<QuarterResponse>>

	@GET("quarters")
	suspend fun getQuarter(): Response<QuarterResponse>

	@DELETE("quarters")
	suspend fun deleteQuarter(
		@Query("qid") quarterId: String
	): Response<Unit>

	@POST("quarters")
	suspend fun addQuarters(
		@Body request: List<AddQuarterRequest>
	): Response<List<QuarterResponse>>
}