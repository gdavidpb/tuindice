package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import com.gdavidpb.tuindice.record.data.source.api.response.AddSyntheticTermRequest
import com.gdavidpb.tuindice.record.data.source.api.response.DeleteOverlayMutationRequest
import com.gdavidpb.tuindice.record.data.source.api.response.SyntheticTermLoadPreviewResponse
import com.gdavidpb.tuindice.record.data.source.api.response.UpsertAttemptOverrideRequest
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview

fun AcademicRecordResponse.toVersionedAcademicRecord(): VersionedAcademicRecord {
	return VersionedAcademicRecord(
		revision = revision,
		record = record
	)
}

fun buildAcademicUpsertAttemptOverrideRequest(
	score: AttemptScore?,
	outcome: AttemptOutcome?,
	mutationId: String,
	expectedRevision: Long
): UpsertAttemptOverrideRequest {
	return UpsertAttemptOverrideRequest(
		score = score,
		outcome = outcome,
		mutationId = mutationId,
		expectedRevision = expectedRevision
	)
}

internal fun AcademicRecordMutation.AddSyntheticTerm.toAddSyntheticTermRequest(
	mutationId: String,
	expectedRevision: Long
): AddSyntheticTermRequest {
	return AddSyntheticTermRequest(
		periodYear = periodYear,
		periodCode = periodCode,
		subjectCodes = attempts.map { attempt -> attempt.subjectCode },
		mutationId = mutationId,
		expectedRevision = expectedRevision
	)
}

fun buildDeleteOverlayMutationRequest(
	mutationId: String,
	expectedRevision: Long
): DeleteOverlayMutationRequest {
	return DeleteOverlayMutationRequest(
		mutationId = mutationId,
		expectedRevision = expectedRevision
	)
}

fun SyntheticTermLoadPreviewResponse.toSyntheticTermLoadPreview(): SyntheticTermLoadPreview {
	return SyntheticTermLoadPreview(
		available = available,
		reason = reason,
		band = band?.let(SyntheticTermLoadBand::valueOf),
		credits = credits,
		weightedDifficulty = weightedDifficulty,
		loadIndex = loadIndex,
		baselineLoadIndex = baselineLoadIndex,
		effectiveTerms = effectiveTerms
	)
}
