package com.gdavidpb.tuindice.evaluations.data.evaluation.source

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.NewEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.UpdateEvaluation

interface RemoteDataSource {
	suspend fun getEvaluation(eid: String): Evaluation
	suspend fun getEvaluations(sid: String): List<Evaluation>
	suspend fun addEvaluation(evaluation: NewEvaluation): Evaluation
	suspend fun updateEvaluation(evaluation: UpdateEvaluation): Evaluation
	suspend fun removeEvaluation(eid: String)
}