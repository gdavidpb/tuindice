package com.gdavidpb.tuindice.evaluations.data.api

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.evaluations.data.api.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.api.mapper.toEvaluationBody
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.RemoteDataSource

class ApiDataSource(
	private val evaluationsApi: EvaluationsApi
) : RemoteDataSource {
	override suspend fun getEvaluation(eid: String): Evaluation {
		return evaluationsApi.getEvaluation(eid)
			.getOrThrow()
			.toEvaluation()
	}

	override suspend fun getEvaluations(sid: String): List<Evaluation> {
		return evaluationsApi.getEvaluations(sid)
			.getOrThrow()
			.map { evaluationResponse -> evaluationResponse.toEvaluation() }
	}

	override suspend fun addEvaluation(evaluation: Evaluation): Evaluation {
		val body = evaluation.toEvaluationBody()

		return evaluationsApi.addEvaluation(body)
			.getOrThrow()
			.toEvaluation()
	}

	override suspend fun removeEvaluation(eid: String) {
		evaluationsApi.deleteEvaluation(eid)
			.getOrThrow()
	}
}