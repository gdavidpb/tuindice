package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import com.gdavidpb.tuindice.record.data.source.api.response.AddSyntheticTermRequest
import com.gdavidpb.tuindice.record.data.source.api.response.SyntheticAttemptRequest
import com.gdavidpb.tuindice.record.data.source.api.response.UpsertAttemptOverrideRequest

fun AcademicRecordResponse.toVersionedAcademicRecord(): VersionedAcademicRecord {
	return VersionedAcademicRecord(
		revision = revision,
		record = record
	)
}

fun buildAcademicUpsertAttemptOverrideRequest(
	score: AttemptScore?,
	outcome: AttemptOutcome?
): UpsertAttemptOverrideRequest {
	return UpsertAttemptOverrideRequest(
		score = score,
		outcome = outcome
	)
}

internal fun AcademicRecordMutation.AddSyntheticTerm.toAddSyntheticTermRequest(): AddSyntheticTermRequest {
	return AddSyntheticTermRequest(
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		attempts = attempts.map { attempt ->
			SyntheticAttemptRequest(
				subjectCode = attempt.subjectCode,
				subjectName = attempt.subjectName,
				credits = attempt.credits,
				gradingMode = attempt.gradingMode,
				score = attempt.score,
				outcome = attempt.outcome
			)
		}
	)
}

internal fun attemptSelectionToOverridePayload(
	grade: Int?,
	status: SubjectStatus?
): Pair<AttemptScore?, AttemptOutcome?> {
	val score = grade?.let { AttemptScore.numeric(it) }
	val outcome = when (status) {
		null -> null
		SubjectStatus.NORMAL -> AttemptOutcome.PENDING
		SubjectStatus.UNREPORTED -> AttemptOutcome.UNREPORTED
		SubjectStatus.APPROVED -> AttemptOutcome.APPROVED
		SubjectStatus.FAILED -> AttemptOutcome.FAILED
		SubjectStatus.RETIRED -> AttemptOutcome.RETIRED
		SubjectStatus.WITHOUT_EFFECT -> null
	}
	return score to outcome
}
