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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal const val CURRENT_ACADEMIC_RECORD_ID = "self"

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

internal fun AddSyntheticTermSeed.toAddSyntheticTermRequest(): AddSyntheticTermRequest {
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

data class AddSyntheticTermSeed(
	val label: String,
	val startAtMillis: Long,
	val endAtMillis: Long,
	val attempts: List<SyntheticAttemptSeed>
)

data class SyntheticAttemptSeed(
	val subjectCode: String,
	val subjectName: String,
	val credits: Int,
	val gradingMode: com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode,
	val score: AttemptScore? = null,
	val outcome: AttemptOutcome? = null
)

internal fun createSyntheticTermSeed(
	quarter: Int,
	year: Int,
	subjects: List<Pair<String, Int?>>
): AddSyntheticTermSeed {
	val (startMonth, endMonth, labelPrefix) = when (quarter) {
		1 -> Triple(1, 3, "Enero - Marzo")
		2 -> Triple(4, 7, "Abril - Julio")
		3 -> Triple(9, 12, "Septiembre - Diciembre")
		else -> Triple(1, 12, "Periodo")
	}
	return AddSyntheticTermSeed(
		label = "$labelPrefix $year",
		startAtMillis = utcMillis(year = year, month = startMonth, day = 1),
		endAtMillis = utcMillis(
			year = year,
			month = endMonth,
			day = when (endMonth) {
				3 -> 31
				7 -> 31
				12 -> 31
				else -> 28
			},
			endOfDay = true
		),
		attempts = subjects.map { (code, grade) ->
			SyntheticAttemptSeed(
				subjectCode = code,
				subjectName = code,
				credits = DEFAULT_SYNTHETIC_ATTEMPT_CREDITS,
				gradingMode = com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode.NUMERIC,
				score = grade?.let(AttemptScore::numeric)
			)
		}
	)
}

private fun utcMillis(
	year: Int,
	month: Int,
	day: Int,
	endOfDay: Boolean = false
): Long {
	val startOfDay = LocalDate(
		year = year,
		monthNumber = month,
		dayOfMonth = day
	).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
	return if (endOfDay) startOfDay + (24 * 60 * 60 * 1000L) - 1 else startOfDay
}

private const val DEFAULT_SYNTHETIC_ATTEMPT_CREDITS = 4
