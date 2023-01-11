package com.gdavidpb.tuindice.evaluations.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Evaluation

interface EvaluationRepository {
	suspend fun getEvaluation(uid: String, eid: String): Evaluation
	suspend fun getEvaluations(uid: String, sid: String): List<Evaluation>
	suspend fun addEvaluation(uid: String, evaluation: Evaluation)
	suspend fun removeEvaluation(uid: String, eid: String)
}