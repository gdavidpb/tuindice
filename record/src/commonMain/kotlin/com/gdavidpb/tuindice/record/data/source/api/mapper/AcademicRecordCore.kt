package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.*
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.source.api.response.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal const val CURRENT_ACADEMIC_RECORD_ID = "self"

fun AcademicRecordResponse.toAcademicRecord(): AcademicRecord {
	val officialTermsDescending = officialProjection.terms.sortedWith(
		compareByDescending<TermProjectionResponse> { it.startAt }
			.thenBy { it.id }
	)
	val officialTermsAscending = officialTermsDescending
		.asReversed()
		.mapIndexed { index, term ->
			term.toAcademicTerm(
				order = index,
				source = AttemptSource.DST_RECORD
			)
		}
	val officialSnapshot = AcademicSnapshot(terms = officialTermsAscending)
	val officialAttemptsById = officialSnapshot.terms
		.flatMap(AcademicTerm::attempts)
		.associateBy(AcademicAttempt::id)
	val simulationTermsDescending = simulationProjection.terms.sortedWith(
		compareByDescending<TermProjectionResponse> { it.startAt }
			.thenBy { it.id }
	)
	val attemptOverrides = simulationTermsDescending
		.flatMap(TermProjectionResponse::attempts)
		.mapNotNull { simulationAttempt ->
			val officialAttempt = officialAttemptsById[simulationAttempt.id] ?: return@mapNotNull null
			if (
				(officialAttempt.officialScore == simulationAttempt.toAttemptScore()) &&
				(officialAttempt.officialOutcome == simulationAttempt.toOfficialOutcome())
			) {
				null
			} else {
				AttemptOverride(
					attemptId = simulationAttempt.id,
					score = simulationAttempt.toAttemptScore(),
					outcome = simulationAttempt.toOfficialOutcome(),
					updatedAtMillis = currentTimeMillis()
				)
			}
		}
	val syntheticTermsAscending = simulationTermsDescending
		.filter { term -> term.kind.isSynthetic }
		.asReversed()
		.mapIndexed { index, term ->
			term.toAcademicTerm(
				order = officialTermsAscending.size + index,
				source = AttemptSource.LOCAL
			)
		}
	val record = AcademicRecord(
		id = CURRENT_ACADEMIC_RECORD_ID,
		revision = revision,
		curriculumKey = "default",
		officialSnapshot = officialSnapshot,
		localOverlay = AcademicOverlay(
			attemptOverrides = attemptOverrides,
			syntheticTerms = syntheticTermsAscending
		),
		updatedAtMillis = currentTimeMillis()
	)

	return AcademicProjectionEngine.reproject(record)
}

private fun TermProjectionResponse.toAcademicTerm(
	order: Int,
	source: AttemptSource
): AcademicTerm {
	return AcademicTerm(
		id = id,
		label = label,
		startAtMillis = startAt,
		endAtMillis = endAt,
		order = order,
		kind = kind,
		attempts = attempts
			.sortedWith(
				compareBy<AttemptProjectionResponse> { it.sequenceInTerm }
					.thenBy { it.id }
			)
			.map { attempt ->
				attempt.toAcademicAttempt(
					source = source,
					termKind = kind
				)
			}
	)
}

private fun AttemptProjectionResponse.toAcademicAttempt(
	source: AttemptSource,
	termKind: TermKind
): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = gradingMode.toAttemptGradingMode(),
		rawGradeToken = "",
		rawObservationText = "",
		officialScore = toAttemptScore(),
		officialOutcome = toOfficialOutcome(),
		officialBadge = badge.toHistoricalBadge(),
		editable = editable,
		source = source,
		synthetic = termKind.isSynthetic
	)
}

fun AttemptProjectionResponse.toUiStatus(): SubjectStatus? {
	if (badge == HistoricalBadgeResponse.WITHOUT_EFFECT) {
		return SubjectStatus.WITHOUT_EFFECT
	}

	return when (outcome) {
		OfficialOutcomeResponse.PENDING -> null
		OfficialOutcomeResponse.APPROVED -> SubjectStatus.APPROVED
		OfficialOutcomeResponse.FAILED -> SubjectStatus.FAILED
		OfficialOutcomeResponse.RETIRED -> SubjectStatus.RETIRED
		OfficialOutcomeResponse.UNREPORTED -> SubjectStatus.UNREPORTED
	}
}

fun buildAcademicUpsertAttemptOverrideRequest(
	score: AttemptScore?,
	outcome: OfficialOutcome?
): UpsertAttemptOverrideRequest {
	return UpsertAttemptOverrideRequest(
		score = score?.toAttemptScoreRequest(),
		outcome = outcome?.toOfficialOutcomeRequest()
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
				gradingMode = attempt.gradingMode.toAttemptGradingModeRequest(),
				score = attempt.score?.toAttemptScoreRequest(),
				outcome = attempt.outcome?.toOfficialOutcomeRequest()
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
				gradingMode = attempt.gradingMode.toAttemptGradingModeRequest(),
				score = attempt.score?.toAttemptScoreRequest(),
				outcome = attempt.outcome?.toOfficialOutcomeRequest()
			)
		}
	)
}

internal fun attemptSelectionToOverridePayload(
	grade: Int?,
	status: SubjectStatus?
): Pair<AttemptScore?, OfficialOutcome?> {
	val score = grade?.let { AttemptScore.numeric(it) }
	val outcome = when (status) {
		null -> null
		SubjectStatus.NORMAL -> OfficialOutcome.PENDING
		SubjectStatus.UNREPORTED -> OfficialOutcome.UNREPORTED
		SubjectStatus.APPROVED -> OfficialOutcome.APPROVED
		SubjectStatus.FAILED -> OfficialOutcome.FAILED
		SubjectStatus.RETIRED -> OfficialOutcome.RETIRED
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
	val gradingMode: AttemptGradingMode,
	val score: AttemptScore? = null,
	val outcome: OfficialOutcome? = null
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
				gradingMode = AttemptGradingMode.NUMERIC,
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
	return if (endOfDay) startOfDay + MILLIS_PER_DAY - 1L else startOfDay
}

private fun AttemptProjectionResponse.toAttemptScore(): AttemptScore {
	return score.toAttemptScore()
}

private fun AttemptScoreResponse.toAttemptScore(): AttemptScore {
	return when (kind) {
		AttemptScoreKindResponse.NUMERIC -> AttemptScore.numeric(numericValue ?: 0)
		AttemptScoreKindResponse.SYMBOLIC -> AttemptScore.symbolic(symbolicValue ?: "")
		AttemptScoreKindResponse.EMPTY -> AttemptScore.empty()
	}
}

private fun AttemptProjectionResponse.toOfficialOutcome(): OfficialOutcome {
	return when (outcome) {
		OfficialOutcomeResponse.PENDING -> OfficialOutcome.PENDING
		OfficialOutcomeResponse.APPROVED -> OfficialOutcome.APPROVED
		OfficialOutcomeResponse.FAILED -> OfficialOutcome.FAILED
		OfficialOutcomeResponse.RETIRED -> OfficialOutcome.RETIRED
		OfficialOutcomeResponse.UNREPORTED -> OfficialOutcome.UNREPORTED
	}
}

private fun HistoricalBadgeResponse.toHistoricalBadge(): HistoricalBadge {
	return when (this) {
		HistoricalBadgeResponse.NONE -> HistoricalBadge.NONE
		HistoricalBadgeResponse.WITHOUT_EFFECT -> HistoricalBadge.WITHOUT_EFFECT
	}
}

private fun AttemptGradingModeResponse.toAttemptGradingMode(): AttemptGradingMode {
	return when (this) {
		AttemptGradingModeResponse.NUMERIC -> AttemptGradingMode.NUMERIC
		AttemptGradingModeResponse.QUALITATIVE_PASS_FAIL -> AttemptGradingMode.QUALITATIVE_PASS_FAIL
	}
}

private fun AttemptScore.toAttemptScoreRequest(): AttemptScoreRequest {
	return AttemptScoreRequest(
		kind = when (kind) {
			AttemptScoreKind.NUMERIC -> AttemptScoreKindRequest.NUMERIC
			AttemptScoreKind.SYMBOLIC -> AttemptScoreKindRequest.SYMBOLIC
			AttemptScoreKind.EMPTY -> AttemptScoreKindRequest.EMPTY
		},
		numericValue = numericValue,
		symbolicValue = symbolicValue
	)
}

private fun OfficialOutcome.toOfficialOutcomeRequest(): OfficialOutcomeRequest {
	return when (this) {
		OfficialOutcome.PENDING -> OfficialOutcomeRequest.PENDING
		OfficialOutcome.APPROVED -> OfficialOutcomeRequest.APPROVED
		OfficialOutcome.FAILED -> OfficialOutcomeRequest.FAILED
		OfficialOutcome.RETIRED -> OfficialOutcomeRequest.RETIRED
		OfficialOutcome.UNREPORTED -> OfficialOutcomeRequest.UNREPORTED
	}
}

private fun AttemptGradingMode.toAttemptGradingModeRequest(): AttemptGradingModeRequest {
	return when (this) {
		AttemptGradingMode.NUMERIC -> AttemptGradingModeRequest.NUMERIC
		AttemptGradingMode.QUALITATIVE_PASS_FAIL -> AttemptGradingModeRequest.QUALITATIVE_PASS_FAIL
	}
}

private const val DEFAULT_SYNTHETIC_ATTEMPT_CREDITS = 4
private const val MILLIS_PER_DAY = 86_400_000L
