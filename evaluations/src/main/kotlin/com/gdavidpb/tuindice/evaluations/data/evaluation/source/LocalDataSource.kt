package com.gdavidpb.tuindice.evaluations.data.evaluation.source

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	suspend fun getEvaluation(uid: String, eid: String): Flow<Evaluation?>
	suspend fun getEvaluations(uid: String, sid: String): Flow<List<Evaluation>>

	suspend fun addEvaluation(uid: String, add: EvaluationAdd)
	suspend fun saveEvaluations(uid: String, evaluations: List<Evaluation>)
	suspend fun removeEvaluation(uid: String, remove: EvaluationRemove)
}