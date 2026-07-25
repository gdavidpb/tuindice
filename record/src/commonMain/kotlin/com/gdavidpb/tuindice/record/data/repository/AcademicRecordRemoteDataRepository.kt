package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview

interface AcademicRecordRemoteDataRepository {
	suspend fun getAcademicRecord(): VersionedAcademicRecord
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord
	suspend fun deleteAttemptOverride(
		attemptId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord
	suspend fun addSyntheticTerm(
		command: AcademicRecordMutation.AddSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord
	suspend fun updateSyntheticTerm(
		command: AcademicRecordMutation.UpdateSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord
	suspend fun deleteSyntheticTerm(
		termId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord
	suspend fun loadSyntheticTermPreview(subjectCodes: List<String>): SyntheticTermLoadPreview {
		error("Synthetic term load preview is not implemented by this repository.")
	}
}
