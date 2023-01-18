package com.gdavidpb.tuindice.evaluations.data.api

import com.gdavidpb.tuindice.evaluations.data.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.response.EvaluationResponse
import retrofit2.Response
import retrofit2.http.*

interface EvaluationsApi {
	@GET("evaluations")
	suspend fun getEvaluation(
		@Query("eid") evaluationId: String
	): Response<EvaluationResponse>

	@GET("evaluations")
	suspend fun getEvaluations(
		@Query("sid") subjectId: String
	): Response<List<EvaluationResponse>>

	@POST("evaluations")
	suspend fun addEvaluation(
		@Body request: AddEvaluationRequest
	): Response<EvaluationResponse>

	@DELETE("evaluations")
	suspend fun deleteEvaluation(
		@Query("eid") evaluationId: String
	): Response<Unit>
}