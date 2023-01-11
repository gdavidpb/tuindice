package com.gdavidpb.tuindice.evaluations.data.evaluation.source

import com.gdavidpb.tuindice.base.domain.model.Evaluation

interface RemoteDataSource {
	suspend fun getEvaluation(eid: String): Evaluation
	suspend fun getEvaluations(sid: String): List<Evaluation>
	suspend fun addEvaluation(evaluation: Evaluation): Evaluation
	suspend fun removeEvaluation(eid: String)
}