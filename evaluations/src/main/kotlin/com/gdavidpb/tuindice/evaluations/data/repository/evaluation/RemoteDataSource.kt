package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

interface RemoteDataSource {
	suspend fun getEvaluations(): List<RemoteEvaluation>
	suspend fun getEvaluation(eid: String): RemoteEvaluation?
	suspend fun addEvaluation(add: EvaluationAdd): RemoteEvaluation
	suspend fun updateEvaluation(update: EvaluationUpdate): RemoteEvaluation
	suspend fun removeEvaluation(remove: EvaluationRemove)
}