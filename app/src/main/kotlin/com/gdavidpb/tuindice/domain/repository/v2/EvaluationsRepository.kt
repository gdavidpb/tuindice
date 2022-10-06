package com.gdavidpb.tuindice.domain.repository.v2

import com.gdavidpb.tuindice.base.domain.model.Evaluation

interface EvaluationsRepository {
	suspend fun addEvaluation(sid: String, evaluation: Evaluation): Evaluation
	suspend fun getEvaluations(sid: String): List<Evaluation>
	suspend fun getEvaluation(eid: String): Evaluation
	suspend fun updateEvaluation(eid: String, evaluation: Evaluation): Evaluation
	suspend fun deleteEvaluation(eid: String)
}