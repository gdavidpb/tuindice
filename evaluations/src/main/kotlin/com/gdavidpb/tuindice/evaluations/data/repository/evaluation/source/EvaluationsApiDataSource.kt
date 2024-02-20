package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.EvaluationsApi
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toAddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toRemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toUpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

class EvaluationsApiDataSource(
	private val evaluationsApi: EvaluationsApi
) : RemoteDataSource {
	override suspend fun getEvaluations(): List<RemoteEvaluation> {
		return evaluationsApi.getEvaluations()
			.getOrThrow()
			.map { evaluationResponse -> evaluationResponse.toRemoteEvaluation() }
	}

	override suspend fun getEvaluation(eid: String): RemoteEvaluation? {
		return runCatching {
			evaluationsApi.getEvaluation(eid)
				.getOrThrow()
				.toRemoteEvaluation()
		}.getOrElse { throwable ->
			if (throwable.isNotFound())
				return null
			else
				throw throwable
		}
	}

	override suspend fun addEvaluation(add: EvaluationAdd): RemoteEvaluation {
		val request = add.toAddEvaluationRequest()

		return evaluationsApi.addEvaluation(request)
			.getOrThrow()
			.toRemoteEvaluation()
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate): RemoteEvaluation {
		val request = update.toUpdateEvaluationRequest()

		return evaluationsApi.updateEvaluation(request)
			.getOrThrow()
			.toRemoteEvaluation()
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		evaluationsApi.deleteEvaluation(remove.evaluationId)
			.getOrThrow()
	}
}