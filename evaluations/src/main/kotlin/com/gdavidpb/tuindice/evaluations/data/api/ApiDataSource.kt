package com.gdavidpb.tuindice.evaluations.data.api

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.evaluations.data.api.mapper.toAddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.api.mapper.toRemoveEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

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

	override suspend fun addEvaluation(add: EvaluationAdd): Evaluation {
		val request = add.toAddEvaluationRequest()

		return evaluationsApi.addEvaluation(request)
			.getOrThrow()
			.toEvaluation()
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate): Evaluation {
		val request = update.toAddEvaluationRequest()

		return evaluationsApi.updateEvaluation(request)
			.getOrThrow()
			.toEvaluation()
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		val request = remove.toRemoveEvaluationRequest()

		evaluationsApi.deleteEvaluation(request)
			.getOrThrow()
	}
}