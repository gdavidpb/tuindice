package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.LocalSubject
import kotlinx.coroutines.flow.Flow

interface DatabaseDataRepository {
	fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>>
	fun observeHasSyncedEvaluationsFlow(): Flow<Boolean>
	suspend fun getEvaluation(eid: String): LocalEvaluation?
	suspend fun getConfirmedSnapshot(): LocalEvaluationsSnapshot
	suspend fun getAvailableSubjects(): List<LocalSubject>
	suspend fun confirmAddedEvaluation(evaluation: LocalEvaluation, anchorRevision: Long): LocalEvaluation
	suspend fun confirmUpdatedEvaluation(evaluation: LocalEvaluation, anchorRevision: Long): LocalEvaluation
	suspend fun confirmRemovedEvaluation(eid: String, anchorRevision: Long)
	suspend fun removeConfirmedEvaluation(eid: String)
	suspend fun saveConfirmedSnapshot(snapshot: LocalEvaluationsSnapshot)
}
