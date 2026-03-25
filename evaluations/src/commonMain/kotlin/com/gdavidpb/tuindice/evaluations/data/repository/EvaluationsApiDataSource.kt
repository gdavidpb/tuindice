package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutationAck

interface EvaluationsApiDataSource {
	suspend fun getEvaluations(): RemoteEvaluationsSnapshot
	suspend fun getEvaluation(eid: String): RemoteEvaluation?
	suspend fun addEvaluation(
		add: EvaluationMutation.Add,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Add
	suspend fun updateEvaluation(
		update: EvaluationMutation.Update,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Update
	suspend fun removeEvaluation(
		eid: String,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Remove
}
