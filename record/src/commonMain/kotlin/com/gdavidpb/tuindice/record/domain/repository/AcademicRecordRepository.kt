package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
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
	suspend fun getAcademicRecord(): AcademicRecord?
	suspend fun updateAcademicRecord()
	suspend fun drainPendingMutations()
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	)
	suspend fun deleteAttemptOverride(attemptId: String)
	suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand)
	suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand)
	suspend fun deleteSyntheticTerm(termId: String)
}
