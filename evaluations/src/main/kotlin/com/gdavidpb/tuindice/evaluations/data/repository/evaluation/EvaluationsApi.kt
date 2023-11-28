package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.response.EvaluationResponse
import com.gdavidpb.tuindice.transactions.domain.annotation.EnqueueOnFailure
import retrofit2.Response
import retrofit2.http.*

interface EvaluationsApi {
	@GET("evaluations")
	suspend fun getEvaluation(
		@Query("eid") evaluationId: String
	): Response<EvaluationResponse>

	@GET("evaluations")
	suspend fun getEvaluations(): Response<List<EvaluationResponse>>

	@EnqueueOnFailure
	@POST("evaluations")
	suspend fun addEvaluation(
		@Body request: AddEvaluationRequest
	): Response<EvaluationResponse>

	@EnqueueOnFailure
	@PATCH("evaluations")
	suspend fun updateEvaluation(
		@Body request: UpdateEvaluationRequest
	): Response<EvaluationResponse>

	@EnqueueOnFailure
	@DELETE("evaluations")
	suspend fun deleteEvaluation(
		@Query("eid") evaluationId: String
	): Response<Unit>
}