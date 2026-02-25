package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import kotlinx.coroutines.flow.Flow

interface DatabaseDataSource {
	fun getEvaluationsFlow(): Flow<List<LocalEvaluation>>
	suspend fun getEvaluation(eid: String): LocalEvaluation?
	suspend fun getAvailableSubjects(): List<LocalSubject>
	suspend fun addEvaluation(evaluation: LocalEvaluation): LocalEvaluation
	suspend fun updateEvaluation(evaluation: LocalEvaluation): LocalEvaluation
	suspend fun removeEvaluation(eid: String)
	suspend fun saveEvaluations(evaluations: List<LocalEvaluation>)
}