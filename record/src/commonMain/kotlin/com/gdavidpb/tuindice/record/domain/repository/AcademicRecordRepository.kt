package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface AcademicRecordRepository {
	suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord>
	suspend fun observeHasSyncedRecordFlow(): Flow<Boolean>
	suspend fun observeAcademicRecordSnapshotFlow(): Flow<ObservedSyncedSnapshot<AcademicRecord>> {
		return combine(
			observeAcademicRecordFlow(),
			observeHasSyncedRecordFlow()
		) { record, hasSyncedRecord ->
			ObservedSyncedSnapshot(
				value = record,
				hasSynced = hasSyncedRecord
			)
		}
	}

	suspend fun observeTerminallyRejectedMutationIdsFlow(): Flow<List<String>>
	suspend fun observeTerminallyRejectedMutationsFlow(): Flow<List<RecordRejection>>
	suspend fun acknowledgeTerminallyRejectedMutations(mutationIds: List<String>)
	suspend fun getAcademicRecord(): AcademicRecord?
	suspend fun updateAcademicRecord()
	suspend fun updateAcademicRecord(forceRemote: Boolean) {
		updateAcademicRecord()
	}
	suspend fun drainPendingMutations()
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String? = null
	)
	suspend fun deleteAttemptOverride(attemptId: String, mutationId: String? = null)
	suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand)
	suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand)
	suspend fun deleteSyntheticTerm(termId: String, mutationId: String? = null)

	suspend fun getRecordRevision(): Long?

	// The terminal results of this repository's writes, as they settle. Nothing is replayed to late subscribers.
	fun observeMutationOutcomesFlow(): Flow<MutationOutcome>
}
