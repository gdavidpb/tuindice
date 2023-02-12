package com.gdavidpb.tuindice.evaluations.data.evaluation.source

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	suspend fun getEvaluation(uid: String, eid: String): Flow<Evaluation?>
	suspend fun getEvaluations(uid: String, sid: String): Flow<List<Evaluation>>

	suspend fun saveEvaluations(uid: String, evaluations: List<Evaluation>)
	suspend fun removeEvaluation(uid: String, eid: String)
}