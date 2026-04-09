package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation

interface AcademicRecordRemoteDataRepository {
	suspend fun getAcademicRecord(): VersionedAcademicRecord
	suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?
	): VersionedAcademicRecord
	suspend fun deleteAttemptOverride(attemptId: String): VersionedAcademicRecord
	suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): VersionedAcademicRecord
	suspend fun deleteSyntheticTerm(termId: String): VersionedAcademicRecord
}
