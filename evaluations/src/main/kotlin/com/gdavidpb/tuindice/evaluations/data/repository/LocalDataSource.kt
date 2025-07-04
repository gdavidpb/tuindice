package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	fun getEvaluationsFlow(uid: String): Flow<List<LocalEvaluation>>
	suspend fun getEvaluation(uid: String, eid: String): LocalEvaluation?
	suspend fun getAvailableSubjects(uid: String): List<LocalSubject>
	suspend fun addEvaluation(uid: String, evaluation: LocalEvaluation): LocalEvaluation
	suspend fun updateEvaluation(uid: String, evaluation: LocalEvaluation): LocalEvaluation
	suspend fun removeEvaluation(uid: String, eid: String)
	suspend fun saveEvaluations(uid: String, evaluations: List<LocalEvaluation>)
}