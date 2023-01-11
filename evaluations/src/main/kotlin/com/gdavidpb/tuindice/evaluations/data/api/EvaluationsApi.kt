package com.gdavidpb.tuindice.evaluations.data.api

import com.gdavidpb.tuindice.evaluations.data.api.bodies.EvaluationBody
import com.gdavidpb.tuindice.evaluations.data.api.responses.EvaluationResponse
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
		@Body evaluationBody: EvaluationBody
	): Response<EvaluationResponse>

	@DELETE("evaluations")
	suspend fun deleteEvaluation(
		@Query("eid") evaluationId: String
	): Response<Unit>
}