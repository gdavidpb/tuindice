package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source

import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toAddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toRemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toUpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.response.EvaluationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class EvaluationsApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun getEvaluations(): List<RemoteEvaluation> {
		return ktorClient.get("evaluations")
			.body<List<EvaluationResponse>>()
			.map { evaluationResponse -> evaluationResponse.toRemoteEvaluation() }
	}

	override suspend fun getEvaluation(eid: String): RemoteEvaluation? {
		return runCatching {
			ktorClient.get("evaluations") {
				parameter("eid", eid)
			}
				.body<EvaluationResponse>()
				.toRemoteEvaluation()
		}.getOrElse { throwable ->
			if (throwable.isNotFound())
				return null
			else
				throw throwable
		}
	}

	override suspend fun addEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation {
		val request = evaluation.toAddEvaluationRequest()

		return ktorClient.post("evaluations") {
			setBody(request)
		}
			.body<EvaluationResponse>()
			.toRemoteEvaluation()
	}

	override suspend fun updateEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation {
		val request = evaluation.toUpdateEvaluationRequest()

		return ktorClient.patch("evaluations") {
			setBody(request)
		}
			.body<EvaluationResponse>()
			.toRemoteEvaluation()
	}

	override suspend fun removeEvaluation(eid: String) {
		ktorClient.delete("evaluations") {
			parameter("eid", eid)
		}
	}
}