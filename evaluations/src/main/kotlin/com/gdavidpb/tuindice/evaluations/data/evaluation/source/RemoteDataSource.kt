package com.gdavidpb.tuindice.evaluations.data.evaluation.source

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

interface RemoteDataSource {
	suspend fun getEvaluation(eid: String): Evaluation
	suspend fun getEvaluations(sid: String): List<Evaluation>
	suspend fun addEvaluation(evaluation: EvaluationAdd): Evaluation
	suspend fun updateEvaluation(evaluation: EvaluationUpdate): Evaluation
	suspend fun removeEvaluation(eid: String)
}