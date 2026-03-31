package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation

sealed interface EvaluationMutationAck {
	data class Add(
		val mutationId: String,
		val anchorRevision: Long,
		val evaluation: RemoteEvaluation
	) : EvaluationMutationAck

	data class Update(
		val mutationId: String,
		val anchorRevision: Long,
		val evaluation: RemoteEvaluation
	) : EvaluationMutationAck

	data class Remove(
		val mutationId: String,
		val anchorRevision: Long,
		val removedEvaluationId: String
	) : EvaluationMutationAck
}
