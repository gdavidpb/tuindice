package com.gdavidpb.tuindice.evaluations.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate

interface EvaluationRepository {
	suspend fun getEvaluation(uid: String, eid: String): Evaluation
	suspend fun getEvaluations(uid: String, sid: String): List<Evaluation>
	suspend fun addEvaluation(uid: String, add: EvaluationAdd): Evaluation
	suspend fun updateEvaluation(uid: String, update: EvaluationUpdate): Evaluation
	suspend fun removeEvaluation(uid: String, eid: String)
}