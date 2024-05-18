package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

interface RemoteDataSource {
	suspend fun getEvaluations(): List<RemoteEvaluation>
	suspend fun getEvaluation(eid: String): RemoteEvaluation?
	suspend fun addEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation
	suspend fun updateEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation
	suspend fun removeEvaluation(eid: String)
}