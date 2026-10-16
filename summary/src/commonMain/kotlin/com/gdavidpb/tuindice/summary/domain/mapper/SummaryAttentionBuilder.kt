package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationOrdinalEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import kotlinx.datetime.LocalDate

private const val MAX_ATTENTION_POINTS = 3

// The ungraded ones go first (they are the most urgent and there is a single line for all), then the
// subjects the university refused part of, by code; three at most.
internal fun attentionOf(
	term: AcademicTerm,
	evaluations: List<Evaluation>,
	today: LocalDate
): List<SummaryAttention> {
	val ungraded = ungradedEvaluationsOf(evaluations = evaluations, today = today)
	val enrollmentErrors = term.attempts
		.filter { attempt -> !attempt.withdrawn }
		.mapNotNull { attempt ->
			attempt.enrollmentErrors
				.orEmpty()
				.map { error -> error.trim() }
				.firstOrNull { error -> error.isNotEmpty() }
				?.let { error ->
					SummaryAttention.EnrollmentError(
						subjectCode = attempt.subjectCode,
						error = error,
						attemptId = attempt.id
					)
				}
		}
		.sortedBy(SummaryAttention.EnrollmentError::subjectCode)

	return (listOfNotNull(ungraded) + enrollmentErrors).take(MAX_ATTENTION_POINTS)
}

// The oldest is the first by its day, then by subject code, its number on the Evaluations tab and its
// id: the order the week lists use, so the same evaluation is the one the jump lands on from any line.
private fun ungradedEvaluationsOf(
	evaluations: List<Evaluation>,
	today: LocalDate
): SummaryAttention.UngradedEvaluations? {
	val overdue = evaluations
		.filter { evaluation -> evaluation.stateOn(today) == EvaluationState.OVERDUE }
		.mapNotNull { evaluation -> evaluation.date?.toLocalDate()?.let { date -> evaluation to date } }

	val ordinals = EvaluationOrdinalEngine.ordinalsById(evaluations)
	val (oldest, oldestDate) = overdue.minWithOrNull(
		compareBy<Pair<Evaluation, LocalDate>>(
			{ (_, date) -> date },
			{ (evaluation, _) -> evaluation.subjectCode },
			{ (evaluation, _) -> ordinals[evaluation.id] ?: 1 },
			{ (evaluation, _) -> evaluation.id }
		)
	) ?: return null

	return SummaryAttention.UngradedEvaluations(
		count = overdue.size,
		oldestWeekStart = AcademicWeekEngine.mondayOf(oldestDate),
		oldestEvaluationId = oldest.id
	)
}
