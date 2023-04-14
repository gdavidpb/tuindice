package com.gdavidpb.tuindice.evaluations.data.api

import com.gdavidpb.tuindice.evaluations.data.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.response.EvaluationResponse
import com.gdavidpb.tuindice.transactions.domain.annotation.EnqueueOnFailure
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

	@EnqueueOnFailure
	@POST("evaluations")
	suspend fun addEvaluation(
		@Body request: AddEvaluationRequest
	): Response<EvaluationResponse>

	@PATCH("evaluations")
	suspend fun updateEvaluation(
		@Body request: UpdateEvaluationRequest
	): Response<EvaluationResponse>

	@DELETE("evaluations")
	suspend fun deleteEvaluation(
		@Query("eid") evaluationId: String
	): Response<Unit>
}