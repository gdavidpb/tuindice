package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.validMeetings
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.record.domain.model.ScheduleEvaluationMarks
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private const val DAYS_PER_WEEK = 7

/**
 * The meetings of these attempts that an evaluation of [evaluations] falls on in the calendar week
 * (Monday to Sunday) that holds [today]. An evaluation falls on the meeting it inherits as its place
 * (see [AcademicScheduleEngine.inheritedMeeting], the one rule): a continuous one, one without a
 * date, one of another week and one on a day its subject has no class inherit none, and mark
 * nothing. Withdrawn attempts are not in the schedule and an evaluation of an attempt that is not
 * here is left out. Its date is read in [timeZone], the way the Evaluations tab reads it.
 */
internal fun List<AttemptProjection>.evaluationMarksOn(
	evaluations: List<Evaluation>,
	today: LocalDate,
	timeZone: TimeZone = TimeZone.currentSystemDefault()
): ScheduleEvaluationMarks {
	val weekStart = today.startOfCalendarWeek()
	val week = weekStart..weekStart.plus(DatePeriod(days = DAYS_PER_WEEK - 1))
	val meetingsByAttempt = filterNot { attempt -> attempt.withdrawn }
		.associate { attempt -> attempt.id to attempt.validMeetings() }
	val marked = evaluations.mapNotNull { evaluation ->
		val date = evaluation.date
			?.let { millis -> Instant.fromEpochMilliseconds(millis).toLocalDateTime(timeZone).date }
			?.takeIf { day -> day in week }

		meetingsByAttempt[evaluation.attemptId]?.let { meetings ->
			AcademicScheduleEngine.inheritedMeeting(
				attemptMeetings = meetings,
				scheduleMode = evaluation.scheduleMode,
				date = date
			)
		}
	}

	return ScheduleEvaluationMarks(weekStart = weekStart, meetings = marked.toSet())
}

/** The Monday of the Monday-to-Sunday week that holds this date. */
internal fun LocalDate.startOfCalendarWeek(): LocalDate = minus(DatePeriod(days = dayOfWeek.ordinal))
