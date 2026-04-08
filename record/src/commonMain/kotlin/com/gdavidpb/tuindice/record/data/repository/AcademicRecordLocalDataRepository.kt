package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.OfficialOutcome
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import kotlinx.coroutines.flow.Flow

interface AcademicRecordLocalDataRepository {
	fun observeAcademicRecordFlow(): Flow<AcademicRecord?>
	suspend fun getAcademicRecord(): AcademicRecord?
	suspend fun saveAcademicRecord(record: AcademicRecord)
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: OfficialOutcome?,
		committed: Boolean
	): AcademicRecord?
	suspend fun deleteAttemptOverride(attemptId: String): AcademicRecord?
	suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): AcademicRecord?
	suspend fun deleteSyntheticTerm(termId: String): AcademicRecord?
}
