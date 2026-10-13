package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationOrdinalEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationPointsEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationStateEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.currentTermOrNull
import com.gdavidpb.tuindice.academiccore.domain.engine.validMeetings
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMoment
import com.gdavidpb.tuindice.academiccore.domain.model.classesStartLocalDate
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.domain.model.SummaryClassMoment
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeekEvaluation
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val MAX_ATTENTION_POINTS = 3
private const val LAST_DAY_OF_WEEK_OFFSET = 6

/**
 * Reads everything the hub says from what is stored and the moment it is asked, with no clock of its
 * own: [now] carries both the instant and the day, and the state of every evaluation is told for
 * that day. Without a ready record or a current term in it there is nothing to say about the term,
 * and only the career remains.
 */
fun buildSummaryHub(
	record: AcademicRecord?,
	isRecordReady: Boolean,
	evaluations: ObservedSyncedSnapshot<List<Evaluation>>,
	now: ScheduleNow,
	pensumProgress: PensumCreditsProgress?
): SummaryHub {
	val term = record
		?.takeIf { isRecordReady }
		?.terms
		?.currentTermOrNull(kind = AcademicTerm::kind, termOrder = AcademicTerm::termOrder, id = AcademicTerm::id)
		?: return SummaryHub.Empty.copy(career = pensumProgress)

	// Those of an attempt the record no longer has (or one that was withdrawn, or is not graded in
	// numbers) are not the term's: they stay out of everything below.
	val subjects = term.attempts.filter { attempt -> attempt.isEvaluable() }
	val subjectIds = subjects.mapTo(HashSet(), AcademicAttempt::id)
	val termEvaluations = evaluations.value.filter { evaluation -> evaluation.attemptId in subjectIds }
	val meetingsByAttempt = term.attempts.associate { attempt -> attempt.id to attempt.validMeetings() }

	return SummaryHub(
		attention = attentionOf(term = term, evaluations = termEvaluations, today = now.date),
		week = weekOf(
			evaluations = termEvaluations,
			meetingsByAttempt = meetingsByAttempt,
			now = now
		),
		term = termOf(
			term = term,
			subjects = subjects,
			evaluations = termEvaluations,
			hasSyncedEvaluations = evaluations.hasSynced,
			today = now.date
		),
		career = pensumProgress
	)
}

private fun AcademicAttempt.isEvaluable(): Boolean {
	return !withdrawn && gradingMode == AttemptGradingMode.NUMERIC
}

// The ungraded ones go first (they are the most urgent and there is a single line for all), then the
// subjects the university refused part of, by code; three at most.
private fun attentionOf(
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
				?.let { error -> SummaryAttention.EnrollmentError(subjectCode = attempt.subjectCode, error = error) }
		}
		.sortedBy(SummaryAttention.EnrollmentError::subjectCode)

	return (listOfNotNull(ungraded) + enrollmentErrors).take(MAX_ATTENTION_POINTS)
}

private fun ungradedEvaluationsOf(
	evaluations: List<Evaluation>,
	today: LocalDate
): SummaryAttention.UngradedEvaluations? {
	val overdueDates = evaluations
		.filter { evaluation -> evaluation.stateOn(today) == EvaluationState.OVERDUE }
		.mapNotNull { evaluation -> evaluation.date?.toLocalDate() }

	val oldest = overdueDates.minOrNull() ?: return null

	return SummaryAttention.UngradedEvaluations(
		count = overdueDates.size,
		oldestWeekStart = AcademicWeekEngine.mondayOf(oldest)
	)
}

// The state stored with an evaluation does not advance with the clock: it is told again for [today].
private fun Evaluation.stateOn(today: LocalDate): EvaluationState {
	return EvaluationStateEngine.stateOf(
		scheduleMode = scheduleMode,
		hasGrade = grade != null,
		date = date?.toLocalDate(),
		today = today
	)
}

private fun weekOf(
	evaluations: List<Evaluation>,
	meetingsByAttempt: Map<String, List<ScheduleMeeting>>,
	now: ScheduleNow
): SummaryWeek? {
	val classMoment = AcademicScheduleEngine
		.currentOrNext(
			meetings = meetingsByAttempt.values.flatten(),
			dayOfWeek = now.dayOfWeek,
			minuteOfDay = now.minuteOfDay
		)
		?.toClassMoment(today = now.date)
	val weekEvaluations = weekEvaluationsOf(
		evaluations = evaluations,
		meetingsByAttempt = meetingsByAttempt,
		today = now.date
	)

	return if (classMoment == null && weekEvaluations.isEmpty()) {
		null
	} else {
		SummaryWeek(
			classMoment = classMoment,
			evaluations = weekEvaluations,
			hasSchedule = meetingsByAttempt.values.any { meetings -> meetings.isNotEmpty() }
		)
	}
}

private fun ScheduleMoment.toClassMoment(today: LocalDate): SummaryClassMoment {
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
private fun weekEvaluationsOf(
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
				)
			)
		}
		.sortedWith(
			compareBy(SummaryWeekEvaluation::date)
				.thenBy(SummaryWeekEvaluation::subjectCode)
				.thenBy(SummaryWeekEvaluation::ordinal)
				.thenBy(SummaryWeekEvaluation::evaluationId)
		)
}

private fun termOf(
	term: AcademicTerm,
	subjects: List<AcademicAttempt>,
	evaluations: List<Evaluation>,
	hasSyncedEvaluations: Boolean,
	today: LocalDate
): SummaryTerm {
	val points = EvaluationPointsEngine.pointsByAttempt(evaluations)

	return SummaryTerm(
		periodCode = term.periodCode,
		periodYear = term.periodYear,
		academicWeek = AcademicWeekEngine.weekOrNull(
			weekOneStart = AcademicWeekEngine.weekOneStart(
				periodYear = term.periodYear,
				periodCode = term.periodCode,
				classesStartDate = term.classesStartLocalDate()
			),
			date = today
		),
		// Nothing downloaded yet is not "no grades": it is not known, so no subject says anything.
		subjects = if (hasSyncedEvaluations) {
			subjects
				.sortedBy(AcademicAttempt::subjectCode)
				.map { attempt ->
					SummarySubjectPoints(
						subjectCode = attempt.subjectCode,
						points = points[attempt.id]?.takeIf { attemptPoints -> attemptPoints.gradedCount > 0 }
					)
				}
		} else {
			emptyList()
		}
	)
}
