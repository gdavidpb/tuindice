package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import kotlinx.coroutines.flow.Flow

interface AcademicRecordRepository {
	suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord>
	suspend fun observeHasSyncedRecordFlow(): Flow<Boolean>
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
	suspend fun deleteSyntheticTerm(termId: String)
}
