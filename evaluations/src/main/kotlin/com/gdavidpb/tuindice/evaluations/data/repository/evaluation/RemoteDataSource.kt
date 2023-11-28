package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

interface RemoteDataSource {
	suspend fun getEvaluations(): List<Evaluation>
	suspend fun getEvaluation(eid: String): Evaluation?
	suspend fun addEvaluation(add: EvaluationAdd): Evaluation
	suspend fun updateEvaluation(update: EvaluationUpdate): Evaluation
	suspend fun removeEvaluation(remove: EvaluationRemove)
}