package com.gdavidpb.tuindice.evaluations.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

interface EvaluationRepository {
	suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>>
	suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean>
	suspend fun observeEvaluationsSnapshotFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return combine(
			observeEvaluationsFlow(),
			observeHasSyncedEvaluationsFlow()
		) { evaluations, hasSyncedEvaluations ->
			ObservedSyncedSnapshot(
				value = evaluations,
				hasSynced = hasSyncedEvaluations
			)
		}
	}
	suspend fun getEvaluationsSnapshot(): ObservedSyncedSnapshot<List<Evaluation>> {
		return observeEvaluationsSnapshotFlow().first()
	}
	suspend fun updateEvaluations(): EvaluationsRefreshResult
	suspend fun updateEvaluations(forceRemote: Boolean): EvaluationsRefreshResult {
		return updateEvaluations()
	}
	suspend fun drainPendingMutations()
	suspend fun getEvaluation(eid: String): Evaluation?
	suspend fun addEvaluation(add: EvaluationAdd)
	suspend fun updateEvaluation(update: EvaluationUpdate)
	suspend fun removeEvaluation(remove: EvaluationRemove)

	suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor>
	suspend fun getCurrentTerm(): EvaluationTermDescriptor?

	/**
	 * The attempts [getAvailableAttempts] returns, observed: it emits again when the current term
	 * changes or when its numeric attempts do (a withdrawal, an enrollment correction), never twice
	 * for the same list. A record write that changes the term and its attempts together is read after
	 * it commits, so it emits the new term with its own attempts and not the old ones.
	 */
	fun observeAvailableAttemptsFlow(): Flow<List<EditableAttemptDescriptor>>

	/**
	 * The current term, observed: it emits again when the current term changes or when its data does
	 * (the first day of classes arrives with a sync), and it emits null while there is none.
	 */
	fun observeCurrentTermFlow(): Flow<EvaluationTermDescriptor?>
}
