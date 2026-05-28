package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import kotlinx.coroutines.flow.Flow

interface DatabaseDataRepository {
	fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>>
	fun observeHasSyncedEvaluationsFlow(): Flow<Boolean>
	suspend fun getEvaluation(eid: String): LocalEvaluation?
	suspend fun getConfirmedSnapshot(): LocalEvaluationsSnapshot
	suspend fun getAvailableAttempts(): List<LocalEditableAttemptDescriptor>
	suspend fun confirmAddedEvaluation(evaluation: LocalEvaluation): LocalEvaluation
	suspend fun confirmUpdatedEvaluation(evaluation: LocalEvaluation): LocalEvaluation
	suspend fun confirmRemovedEvaluation(eid: String)
	suspend fun removeConfirmedEvaluation(eid: String)
	suspend fun saveConfirmedSnapshot(snapshot: LocalEvaluationsSnapshot)
}
