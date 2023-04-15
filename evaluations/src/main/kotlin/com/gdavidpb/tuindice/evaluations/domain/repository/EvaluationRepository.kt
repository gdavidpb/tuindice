package com.gdavidpb.tuindice.evaluations.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
	suspend fun getEvaluation(uid: String, eid: String): Flow<Evaluation>
	suspend fun getEvaluations(uid: String, sid: String): Flow<List<Evaluation>>

	suspend fun addEvaluation(uid: String, add: EvaluationAdd)
	suspend fun updateEvaluation(uid: String, update: EvaluationUpdate): Evaluation
	suspend fun removeEvaluation(uid: String, remove: EvaluationRemove)
}