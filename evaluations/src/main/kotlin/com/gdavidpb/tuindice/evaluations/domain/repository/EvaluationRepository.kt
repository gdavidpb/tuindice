package com.gdavidpb.tuindice.evaluations.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.NewEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.UpdateEvaluation

interface EvaluationRepository {
	suspend fun getEvaluation(uid: String, eid: String): Evaluation
	suspend fun getEvaluations(uid: String, sid: String): List<Evaluation>
	suspend fun addEvaluation(uid: String, evaluation: NewEvaluation): Evaluation
	suspend fun updateEvaluation(uid: String, eid: String, evaluation: UpdateEvaluation): Evaluation
	suspend fun removeEvaluation(uid: String, eid: String)
}