package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	suspend fun getEvaluations(uid: String): Flow<List<LocalEvaluation>>
	suspend fun getEvaluation(uid: String, eid: String): LocalEvaluation?
	suspend fun getAvailableSubjects(uid: String): List<Subject>

	suspend fun addEvaluation(uid: String, add: EvaluationAdd)
	suspend fun updateEvaluation(uid: String, update: EvaluationUpdate)
	suspend fun removeEvaluation(uid: String, remove: EvaluationRemove)

	suspend fun saveEvaluations(uid: String, evaluations: List<LocalEvaluation>)
}