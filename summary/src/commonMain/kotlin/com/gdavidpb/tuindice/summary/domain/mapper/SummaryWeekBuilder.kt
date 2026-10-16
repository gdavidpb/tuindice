package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationOrdinalEngine
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMoment
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.summary.domain.model.SummaryClassMoment
import com.gdavidpb.tuindice.summary.domain.model.SummaryNextEvaluation
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeekEvaluation
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val LAST_DAY_OF_WEEK_OFFSET = 6

internal fun ScheduleMoment.toClassMoment(today: LocalDate): SummaryClassMoment {
	return when (this) {
		is ScheduleMoment.InProgress -> SummaryClassMoment(
			meeting = meeting,
			isInProgress = true,
			date = today,
			isToday = true
		)

		is ScheduleMoment.Next -> SummaryClassMoment(
			meeting = meeting,
			isInProgress = false,
			date = today.plus(DatePeriod(days = daysAhead)),
			isToday = daysAhead == 0
		)
	}
}

// The dated evaluations from today to the Sunday that closes this calendar week. A continuous one
// has no day, and one of an earlier day has gone by.
internal fun weekEvaluationsOf(
	evaluations: List<Evaluation>,
	meetingsByAttempt: Map<String, List<ScheduleMeeting>>,
	today: LocalDate
): List<SummaryWeekEvaluation> {
	val weekEnd = AcademicWeekEngine.mondayOf(today).plus(DatePeriod(days = LAST_DAY_OF_WEEK_OFFSET))
	// The number the Evaluations tab gives each evaluation, which must not depend on this week's filter.
	val ordinals = EvaluationOrdinalEngine.ordinalsById(evaluations)

	return evaluations
		.filter { evaluation -> evaluation.scheduleMode == EvaluationScheduleMode.DATED }
		.mapNotNull { evaluation ->
			val date = evaluation.date?.toLocalDate() ?: return@mapNotNull null

			if (date < today || date > weekEnd) return@mapNotNull null

			SummaryWeekEvaluation(
				evaluationId = evaluation.id,
				subjectCode = evaluation.subjectCode,
				type = evaluation.type,
				ordinal = ordinals[evaluation.id] ?: 1,
				date = date,
				isToday = date == today,
				meeting = AcademicScheduleEngine.inheritedMeeting(
					attemptMeetings = meetingsByAttempt[evaluation.attemptId].orEmpty(),
					scheduleMode = evaluation.scheduleMode,
					date = date
				),
				state = evaluation.stateOn(today)
			)
		}
		.sortedWith(
			compareBy(SummaryWeekEvaluation::date)
				.thenBy(SummaryWeekEvaluation::subjectCode)
				.thenBy(SummaryWeekEvaluation::ordinal)
				.thenBy(SummaryWeekEvaluation::evaluationId)
		)
}

// The earliest dated evaluation of the term with no grade that falls from tomorrow on, whatever its subject. A tie on
// the day goes to the one the week lists put first (subject, number, then id), so every line names the same one.
internal fun upcomingEvaluationOf(
	evaluations: List<Evaluation>,
	today: LocalDate
): SummaryNextEvaluation? {
	val ordinals = EvaluationOrdinalEngine.ordinalsById(evaluations)

	return evaluations
		.filter { evaluation -> evaluation.scheduleMode == EvaluationScheduleMode.DATED }
		.filter { evaluation -> evaluation.stateOn(today) == EvaluationState.PENDING }
		.mapNotNull { evaluation -> evaluation.date?.toLocalDate()?.let { date -> evaluation to date } }
		.filter { (_, date) -> date > today }
		.minWithOrNull(
			compareBy<Pair<Evaluation, LocalDate>>(
				{ (_, date) -> date },
				{ (evaluation, _) -> evaluation.subjectCode },
				{ (evaluation, _) -> ordinals[evaluation.id] ?: 1 },
				{ (evaluation, _) -> evaluation.id }
			)
		)
		?.let { (next, day) ->
			SummaryNextEvaluation(
				evaluationId = next.id,
				subjectCode = next.subjectCode,
				type = next.type,
				ordinal = ordinals[next.id] ?: 1,
				date = day,
				isToday = false
			)
		}
}
