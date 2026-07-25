package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.LocalCurrentTermDescriptor
import kotlinx.coroutines.flow.Flow

interface DatabaseDataRepository {
	fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>>
	fun observeHasSyncedEvaluationsFlow(): Flow<Boolean>
	fun observeEvaluationsSnapshotFlow(): Flow<LocalEvaluationsSnapshot>
	suspend fun getEvaluation(eid: String): LocalEvaluation?
	suspend fun getEvaluationsSnapshot(): LocalEvaluationsSnapshot
	suspend fun getConfirmedSnapshot(): LocalEvaluationsSnapshot
	suspend fun getAvailableAttempts(): List<LocalEditableAttemptDescriptor>
	suspend fun getCurrentTerm(): LocalCurrentTermDescriptor?
	suspend fun confirmAddedEvaluation(evaluation: LocalEvaluation): LocalEvaluation
	suspend fun confirmUpdatedEvaluation(evaluation: LocalEvaluation): LocalEvaluation
	suspend fun confirmEvaluationRemoval(eid: String)
	suspend fun discardLocalEvaluationCopy(eid: String)
	suspend fun saveConfirmedSnapshot(snapshot: LocalEvaluationsSnapshot)
}
