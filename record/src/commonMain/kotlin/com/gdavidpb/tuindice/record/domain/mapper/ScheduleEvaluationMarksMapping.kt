package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.validMeetings
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.record.domain.model.ScheduleEvaluationMarks
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

/**
 * The meetings of these attempts that an evaluation of [evaluations] falls on in the calendar week
 * (Monday to Sunday) that holds [today], each mapped to the id of the evaluation that marks it (see
 * [ScheduleEvaluationMarks.evaluationIdByMeeting]). An evaluation falls on the meeting it inherits as
 * its place (see [AcademicScheduleEngine.inheritedMeeting], the one rule): a continuous one, one
 * without a date, one of another week and one on a day its subject has no class inherit none, and
 * mark nothing. When several fall on the same meeting, the mark goes to the earliest of them by day
 * and, on a tie of day, to the smallest id. Withdrawn attempts are not in the schedule and an
 * evaluation of an attempt that is not here is left out. Its date is read in [timeZone], the way the
 * Evaluations tab reads it.
 */
internal fun List<AttemptProjection>.evaluationMarksOn(
	evaluations: List<Evaluation>,
	today: LocalDate,
	timeZone: TimeZone = TimeZone.currentSystemDefault()
): ScheduleEvaluationMarks {
	val weekStart = AcademicWeekEngine.mondayOf(today)
	val week = weekStart..weekStart.plus(DatePeriod(days = AcademicWeekEngine.DAYS_PER_WEEK - 1))
	val meetingsByAttempt = associate { attempt -> attempt.id to attempt.validMeetings() }
	val marked = evaluations.mapNotNull { evaluation ->
		val date = evaluation.date
			?.toLocalDate(timeZone)
			?.takeIf { day -> day in week }
		val meeting = meetingsByAttempt[evaluation.attemptId]?.let { meetings ->
			AcademicScheduleEngine.inheritedMeeting(
				attemptMeetings = meetings,
				scheduleMode = evaluation.scheduleMode,
				date = date
			)
		}

		if (meeting != null && date != null) {
			MarkedEvaluation(meeting = meeting, date = date, id = evaluation.id)
		} else {
			null
		}
	}
	val evaluationIdByMeeting = marked
		.groupBy(MarkedEvaluation::meeting)
		.mapValues { (_, evaluationsOfMeeting) -> evaluationsOfMeeting.minWith(markOrder).id }

	return ScheduleEvaluationMarks(weekStart = weekStart, evaluationIdByMeeting = evaluationIdByMeeting)
}

// An evaluation that falls on a meeting of the week, with the day of the week it is on.
private data class MarkedEvaluation(
	val meeting: ScheduleMeeting,
	val date: LocalDate,
	val id: String
)

// The mark of a meeting goes to the first of its evaluations in this order: the earliest day, and the
// smallest id on a tie of day. A meeting falls on one day of the week, so in practice the id decides;
// the day comes first all the same, so the rule does not depend on that.
private val markOrder = compareBy(MarkedEvaluation::date, MarkedEvaluation::id)
