package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.evaluations.data.mapper.toAddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.mapper.toMutationAck
import com.gdavidpb.tuindice.evaluations.data.mapper.toRemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toRemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mapper.toUpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.DeleteEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.DeleteEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.GetEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.GetEvaluationsResponse
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutationAck
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.appendPathSegments

class KtorEvaluationsApiDataSource(
	private val ktorClient: HttpClient
) : EvaluationsApiDataSource {
	override suspend fun getEvaluations(): RemoteEvaluationsSnapshot {
		return ktorClient.get("evaluations/v2")
			.body<GetEvaluationsResponse>()
			.toRemoteEvaluationsSnapshot()
	}

	override suspend fun getEvaluation(eid: String): RemoteEvaluation? {
		return runCatching {
			ktorClient.get("evaluations/v2") {
				url { appendPathSegments(eid) }
			}
				.body<GetEvaluationResponse>()
				.evaluation
				.toRemoteEvaluation()
		}.getOrElse { throwable ->
			if (throwable.isNotFound()) {
				null
			} else {
				throw throwable
			}
		}
	}

	override suspend fun addEvaluation(
		add: EvaluationMutation.Add,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Add {
		return ktorClient.post("evaluations/v2") {
			setBody(
				add.toAddEvaluationRequest(
					mutationId = mutationId,
					expectedRevision = expectedRevision
				)
			)
		}
			.body<AddEvaluationResponse>()
			.toMutationAck()
	}

	override suspend fun updateEvaluation(
		update: EvaluationMutation.Update,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Update {
		return ktorClient.patch("evaluations/v2/${update.evaluationId}") {
			setBody(
				update.toUpdateEvaluationRequest(
					mutationId = mutationId,
					expectedRevision = expectedRevision
				)
			)
		}
			.body<UpdateEvaluationResponse>()
			.toMutationAck()
	}

	override suspend fun removeEvaluation(
		eid: String,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Remove {
		return ktorClient.delete("evaluations/v2/$eid") {
			setBody(
				DeleteEvaluationRequest(
					mutationId = mutationId,
					expectedRevision = expectedRevision
				)
			)
		}
			.body<DeleteEvaluationResponse>()
			.toMutationAck()
	}
}
