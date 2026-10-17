@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttention
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The points of attention of a term. [timeZone] is the zone the dates of the evaluations are read in: the same
 * instant is one day in one zone and the next in another, so the caller says which one, and [today] must be
 * the day in that same zone.
 */
object AcademicAttentionEngine {
	private const val MAX_ATTENTION_POINTS = 3

	// The ungraded ones go first (they are the most urgent and there is a single line for all), then the
	// subjects the university refused part of, by code; three at most.
	fun attentionOf(
		term: AcademicTerm,
		evaluations: List<Evaluation>,
		today: LocalDate,
		timeZone: TimeZone
	): List<AcademicAttention> {
		val ungraded = ungradedEvaluationsOf(evaluations = evaluations, today = today, timeZone = timeZone)
		val enrollmentErrors = term.attempts
			.filter { attempt -> !attempt.withdrawn }
			.mapNotNull { attempt ->
				attempt.enrollmentErrors
					.orEmpty()
					.map { error -> error.trim() }
					.firstOrNull { error -> error.isNotEmpty() }
					?.let { error ->
						AcademicAttention.EnrollmentError(
							subjectCode = attempt.subjectCode,
							error = error,
							attemptId = attempt.id
						)
					}
			}
			.sortedBy(AcademicAttention.EnrollmentError::subjectCode)

		return (listOfNotNull(ungraded) + enrollmentErrors).take(MAX_ATTENTION_POINTS)
	}

	// The oldest is the first by its day, then by subject code, its number on the Evaluations tab and its
	// id: the order the week lists use, so the same evaluation is the one the jump lands on from any line.
	private fun ungradedEvaluationsOf(
		evaluations: List<Evaluation>,
		today: LocalDate,
		timeZone: TimeZone
	): AcademicAttention.UngradedEvaluations? {
		val overdue = evaluations
			.filter { evaluation -> evaluation.stateOn(today = today, timeZone = timeZone) == EvaluationState.OVERDUE }
			.mapNotNull { evaluation -> evaluation.date?.dayIn(timeZone)?.let { date -> evaluation to date } }

		val ordinals = EvaluationOrdinalEngine.ordinalsById(evaluations)
		val (oldest, oldestDate) = overdue.minWithOrNull(
			compareBy<Pair<Evaluation, LocalDate>>(
				{ (_, date) -> date },
				{ (evaluation, _) -> evaluation.subjectCode },
				{ (evaluation, _) -> ordinals[evaluation.id] ?: 1 },
				{ (evaluation, _) -> evaluation.id }
			)
		) ?: return null

		return AcademicAttention.UngradedEvaluations(
			count = overdue.size,
			oldestWeekStart = AcademicWeekEngine.mondayOf(oldestDate),
			oldestEvaluationId = oldest.id
		)
	}

	// The state EvaluationStateEngine tells an evaluation has on [today]; its date is the day it falls on in
	// [timeZone].
	private fun Evaluation.stateOn(today: LocalDate, timeZone: TimeZone): EvaluationState {
		return EvaluationStateEngine.stateOf(
			scheduleMode = scheduleMode,
			hasGrade = grade != null,
			date = date?.dayIn(timeZone),
			today = today
		)
	}

	// The calendar day of this instant (milliseconds since the epoch) in [timeZone].
	private fun Long.dayIn(timeZone: TimeZone): LocalDate {
		return Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date
	}
}
