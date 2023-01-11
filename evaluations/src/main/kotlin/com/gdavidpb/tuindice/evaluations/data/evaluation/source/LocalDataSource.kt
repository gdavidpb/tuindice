package com.gdavidpb.tuindice.evaluations.data.evaluation.source

import com.gdavidpb.tuindice.base.domain.model.Evaluation

interface LocalDataSource {
	suspend fun getEvaluation(uid: String, eid: String): Evaluation
	suspend fun getEvaluations(uid: String, sid: String): List<Evaluation>
	suspend fun saveEvaluations(uid: String, vararg evaluations: Evaluation)
	suspend fun removeEvaluation(uid: String, eid: String)
}