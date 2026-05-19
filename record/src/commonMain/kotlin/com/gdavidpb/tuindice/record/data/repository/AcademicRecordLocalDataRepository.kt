package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import kotlinx.coroutines.flow.Flow

interface AcademicRecordLocalDataRepository {
	fun observeAcademicRecordFlow(): Flow<AcademicRecord?>
	fun observeHasSyncedRecordFlow(): Flow<Boolean>
	suspend fun hasAcademicRecord(): Boolean
	suspend fun getAcademicRecord(): AcademicRecord?
	suspend fun getRecordRevision(): Long?
	suspend fun saveAcademicRecord(record: VersionedAcademicRecord)
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		committed: Boolean
	): AcademicRecord?
	suspend fun deleteAttemptOverride(attemptId: String): AcademicRecord?
	suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): AcademicRecord?
	suspend fun updateSyntheticTerm(command: AcademicRecordMutation.UpdateSyntheticTerm): AcademicRecord?
	suspend fun deleteSyntheticTerm(termId: String): AcademicRecord?
}
