package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.parser.EvaluationAddParser
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.parser.EvaluationRemoveParser
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.parser.EvaluationUpdateParser
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.response.EvaluationResponse
import com.gdavidpb.tuindice.transactions.domain.annotation.EnqueuedApi
import retrofit2.Response
import retrofit2.http.*

interface EvaluationsApi {
	@GET("evaluations")
	suspend fun getEvaluation(
		@Query("eid") evaluationId: String
	): Response<EvaluationResponse>

	@GET("evaluations")
	suspend fun getEvaluations(): Response<List<EvaluationResponse>>

	@EnqueuedApi(EvaluationAddParser::class)
	@POST("evaluations")
	suspend fun addEvaluation(
		@Body request: AddEvaluationRequest
	): Response<EvaluationResponse>

	@EnqueuedApi(EvaluationUpdateParser::class)
	@PATCH("evaluations")
	suspend fun updateEvaluation(
		@Body request: UpdateEvaluationRequest
	): Response<EvaluationResponse>

	@EnqueuedApi(EvaluationRemoveParser::class)
	@DELETE("evaluations")
	suspend fun deleteEvaluation(
		@Query("eid") evaluationId: String
	): Response<Unit>
}