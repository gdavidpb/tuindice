package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.OfficialOutcome
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import kotlinx.coroutines.flow.Flow

interface AcademicRecordRepository {
	suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord>
	suspend fun refreshAcademicRecord()
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: OfficialOutcome?,
		commit: Boolean
	)
	suspend fun deleteAttemptOverride(attemptId: String)
	suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm)
	suspend fun deleteSyntheticTerm(termId: String)
}
