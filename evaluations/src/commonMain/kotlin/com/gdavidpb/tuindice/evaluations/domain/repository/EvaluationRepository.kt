package com.gdavidpb.tuindice.evaluations.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.ObservedEvaluations
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
	suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>>
	suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean>
	suspend fun observeEvaluationsSnapshotFlow(): Flow<ObservedEvaluations>
	suspend fun updateEvaluations()
	suspend fun drainPendingMutations()
	suspend fun getEvaluation(eid: String): Evaluation?
	suspend fun addEvaluation(add: EvaluationAdd)
	suspend fun updateEvaluation(update: EvaluationUpdate)
	suspend fun removeEvaluation(remove: EvaluationRemove)

	suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor>
	suspend fun getCurrentTerm(): EvaluationTermDescriptor?
}
