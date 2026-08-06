package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow

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
	// Defaults: only the real data source owns an outbox; test doubles without one
	// observe no rejections and acknowledge nothing.
	suspend fun observeTerminallyRejectedMutationIdsFlow(): Flow<List<String>> = emptyFlow()
	suspend fun acknowledgeTerminallyRejectedMutations(mutationIds: List<String>) = Unit
	suspend fun getAcademicRecord(): AcademicRecord?
	suspend fun updateAcademicRecord()
	suspend fun updateAcademicRecord(forceRemote: Boolean) {
		updateAcademicRecord()
	}
	suspend fun drainPendingMutations()
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?
	)
	suspend fun deleteAttemptOverride(attemptId: String)
	suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand)
	suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand)
	suspend fun deleteSyntheticTerm(termId: String)
}
