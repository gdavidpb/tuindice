package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationOrdinalEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationPointsEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationStateEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordStatisticsEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.currentTermOrNull
import com.gdavidpb.tuindice.academiccore.domain.engine.validMeetings
import com.gdavidpb.tuindice.academiccore.domain.mapper.classesStartLocalDate
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryFinishedTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryNextEvaluation
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordReading
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordState
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import kotlinx.datetime.LocalDate

/**
 * Reads everything the hub says from what is stored and the moment it is asked, with no clock of its
 * own: [now] carries both the instant and the day, and the state of every evaluation is told for that day.
 *
 * A ready record has both the week and the term: the term in progress when the record has one, and otherwise
 * what the record says of the terms it has (the last one that is over, and whether it has any at all). A new
 * student the university has no record for yet has no term yet. Any other state of the record leaves only the career.
 *
 * This reads the record on its own, once, through [recordReadingOf]. The observed hub reads it once for each emission
 * of the record instead, and builds the hub over that reading for each minute, so a minute that passes never projects
 * the record again.
 */
internal fun buildSummaryHub(
	recordState: SummaryRecordState,
	evaluations: ObservedSyncedSnapshot<List<Evaluation>>,
	now: ScheduleNow,
	pensumProgress: PensumCreditsProgress?
): SummaryHub {
	return buildSummaryHub(
		reading = recordReadingOf(recordState),
		evaluations = evaluations,
		now = now,
		pensumProgress = pensumProgress
	)
}

// The hub over a record already read: none of its parts projects the record again, whatever the moment it is asked on.
internal fun buildSummaryHub(
	reading: SummaryRecordReading,
	evaluations: ObservedSyncedSnapshot<List<Evaluation>>,
	now: ScheduleNow,
	pensumProgress: PensumCreditsProgress?
): SummaryHub {
	// The pensum's progress enters the domain here, as the summary's own figure, and every hub and statistics below
	// carries that figure.
	val career = pensumProgress?.let { progress ->
		SummaryCreditsProgress(approvedCredits = progress.approvedCredits, totalCredits = progress.totalCredits)
	}

	return when (reading) {
		is SummaryRecordReading.Ready -> readyHubOf(
			reading = reading,
			evaluations = evaluations,
			now = now,
			pensumProgress = career
		)

		SummaryRecordReading.NewStudentWithoutRecord -> idleHubOf(
			hasTerms = false,
			finished = null,
			closed = RecordStatisticsEngine.statisticsOf(RecordProjection()),
			pensumProgress = career
		)

		SummaryRecordReading.NotReady -> SummaryHub.Empty.copy(career = career)
	}
}

private fun readyHubOf(
	reading: SummaryRecordReading.Ready,
	evaluations: ObservedSyncedSnapshot<List<Evaluation>>,
	now: ScheduleNow,
	pensumProgress: SummaryCreditsProgress?
): SummaryHub {
	val term = reading.record.terms
		.currentTermOrNull(kind = AcademicTerm::kind, termOrder = AcademicTerm::termOrder, id = AcademicTerm::id)

	return if (term == null) {
		idleHubOf(
			hasTerms = reading.record.terms.isNotEmpty(),
			finished = finishedTermOf(projection = reading.projection, closed = reading.closed),
			closed = reading.closed,
			pensumProgress = pensumProgress
		)
	} else {
		inProgressHubOf(
			term = term,
			evaluations = evaluations,
			now = now,
			pensumProgress = pensumProgress,
			closed = reading.closed
		)
	}
}

// No term in progress: nothing is attended to, and the week and the term say only what the record has of its terms,
// or that there is nothing yet for a new student. The statistics are those of the closed terms alone.
private fun idleHubOf(
	hasTerms: Boolean,
	finished: SummaryFinishedTerm?,
	closed: RecordStatistics,
	pensumProgress: SummaryCreditsProgress?
): SummaryHub {
	return SummaryHub(
		attention = emptyList(),
		week = SummaryWeek.Idle(hasTerms = hasTerms),
		term = SummaryTerm.Idle(hasTerms = hasTerms, finished = finished),
		career = pensumProgress,
		statistics = summaryStatisticsOf(closed = closed, pensumProgress = pensumProgress, currentTerm = null)
	)
}

private fun inProgressHubOf(
	term: AcademicTerm,
	evaluations: ObservedSyncedSnapshot<List<Evaluation>>,
	now: ScheduleNow,
	pensumProgress: SummaryCreditsProgress?,
	closed: RecordStatistics
): SummaryHub {
	// Those of an attempt the record no longer has (or one that was withdrawn, or is not graded in
	// numbers) are not the term's: they stay out of everything below.
	val subjects = term.attempts.filter { attempt -> attempt.isEvaluable() }
	val subjectIds = subjects.mapTo(HashSet(), AcademicAttempt::id)
	val termEvaluations = evaluations.value.filter { evaluation -> evaluation.attemptId in subjectIds }
	val meetingsByAttempt = term.attempts.associate { attempt -> attempt.id to attempt.validMeetings() }
	val termInProgress = termOf(
		term = term,
		subjects = subjects,
		evaluations = termEvaluations,
		hasSyncedEvaluations = evaluations.hasSynced,
		today = now.date
	)

	return SummaryHub(
		attention = attentionOf(term = term, evaluations = termEvaluations, today = now.date),
		week = weekOf(
			evaluations = termEvaluations,
			hasSyncedEvaluations = evaluations.hasSynced,
			meetingsByAttempt = meetingsByAttempt,
			now = now
		),
		term = termInProgress,
		career = pensumProgress,
		// The statistics count the same subjects and evaluations as the term above, so they cannot count another set.
		statistics = summaryStatisticsOf(
			closed = closed,
			pensumProgress = pensumProgress,
			currentTerm = currentTermStatisticsOf(term = termInProgress, evaluations = termEvaluations, today = now.date)
		)
	)
}

private fun AcademicAttempt.isEvaluable(): Boolean {
	return !withdrawn && gradingMode == AttemptGradingMode.NUMERIC
}

// The state stored with an evaluation does not advance with the clock: it is told again for [today].
internal fun Evaluation.stateOn(today: LocalDate): EvaluationState {
	return EvaluationStateEngine.stateOf(
		scheduleMode = scheduleMode,
		hasGrade = grade != null,
		date = date?.toLocalDate(),
		today = today
	)
}

// The week of the term in progress: its class (the one in progress or the next), the evaluations that have not gone by,
// and what the week needs from the rest of the term: whether any evaluation is registered and the next one to come.
private fun weekOf(
	evaluations: List<Evaluation>,
	hasSyncedEvaluations: Boolean,
	meetingsByAttempt: Map<String, List<ScheduleMeeting>>,
	now: ScheduleNow
): SummaryWeek.InProgress {
	val classMoment = AcademicScheduleEngine
		.currentOrNext(
			meetings = meetingsByAttempt.values.flatten(),
			dayOfWeek = now.dayOfWeek,
			minuteOfDay = now.minuteOfDay
		)
		?.toClassMoment(today = now.date)

	return SummaryWeek.InProgress(
		classMoment = classMoment,
		evaluations = weekEvaluationsOf(
			evaluations = evaluations,
			meetingsByAttempt = meetingsByAttempt,
			today = now.date
		),
		hasSchedule = meetingsByAttempt.values.any { meetings -> meetings.isNotEmpty() },
		hasSyncedEvaluations = hasSyncedEvaluations,
		hasEvaluations = evaluations.isNotEmpty(),
		nextEvaluation = upcomingEvaluationOf(evaluations = evaluations, today = now.date)
	)
}

private fun termOf(
	term: AcademicTerm,
	subjects: List<AcademicAttempt>,
	evaluations: List<Evaluation>,
	hasSyncedEvaluations: Boolean,
	today: LocalDate
): SummaryTerm.InProgress {
	val points = EvaluationPointsEngine.pointsByAttempt(evaluations)
	val nextEvaluations = nextEvaluationsOf(evaluations = evaluations, today = today)

	return SummaryTerm.InProgress(
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
						points = points[attempt.id],
						attemptId = attempt.id,
						credits = attempt.credits,
						nextEvaluation = nextEvaluations[attempt.id]
					)
				}
		} else {
			emptyList()
		},
		hasSyncedEvaluations = hasSyncedEvaluations,
		termId = term.id
	)
}

// The next evaluation of each subject: the earliest of its dated evaluations that has no grade and has not gone by.
// A tie on the day goes to the one the week lists put first (its number, then its id), so any line names the same one.
private fun nextEvaluationsOf(
	evaluations: List<Evaluation>,
	today: LocalDate
): Map<String, SummaryNextEvaluation> {
	val ordinals = EvaluationOrdinalEngine.ordinalsById(evaluations)

	return evaluations
		.filter { evaluation -> evaluation.scheduleMode == EvaluationScheduleMode.DATED }
		.filter { evaluation -> evaluation.stateOn(today) == EvaluationState.PENDING }
		.mapNotNull { evaluation -> evaluation.date?.toLocalDate()?.let { date -> evaluation to date } }
		.groupBy { (evaluation, _) -> evaluation.attemptId }
		.mapValues { (_, dated) ->
			val (next, day) = dated.minWith(
				compareBy<Pair<Evaluation, LocalDate>>(
					{ (_, date) -> date },
					{ (evaluation, _) -> ordinals[evaluation.id] ?: 1 },
					{ (evaluation, _) -> evaluation.id }
				)
			)

			SummaryNextEvaluation(
				evaluationId = next.id,
				subjectCode = next.subjectCode,
				type = next.type,
				ordinal = ordinals[next.id] ?: 1,
				date = day,
				isToday = day == today
			)
		}
}
