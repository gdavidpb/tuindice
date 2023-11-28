package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.EvaluationsApi
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toAddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toUpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

class EvaluationsApiDataSource(
	private val evaluationsApi: EvaluationsApi
) : RemoteDataSource {
	override suspend fun getEvaluations(): List<Evaluation> {
		return evaluationsApi.getEvaluations()
			.getOrThrow()
			.map { evaluationResponse -> evaluationResponse.toEvaluation() }
	}

	override suspend fun getEvaluation(eid: String): Evaluation {
		return evaluationsApi.getEvaluation(eid)
			.getOrThrow()
			.toEvaluation()
	}

	override suspend fun addEvaluation(add: EvaluationAdd): Evaluation {
		val request = add.toAddEvaluationRequest()

		return evaluationsApi.addEvaluation(request)
			.getOrThrow()
			.toEvaluation()
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate): Evaluation {
		val request = update.toUpdateEvaluationRequest()

		return evaluationsApi.updateEvaluation(request)
			.getOrThrow()
			.toEvaluation()
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		evaluationsApi.deleteEvaluation(remove.evaluationId)
			.getOrThrow()
	}
}