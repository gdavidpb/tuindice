package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordState
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import com.gdavidpb.tuindice.summary.testing.HUB_CLASSES_START
import com.gdavidpb.tuindice.summary.testing.HUB_THURSDAY
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubDate
import com.gdavidpb.tuindice.summary.testing.hubEvaluation
import com.gdavidpb.tuindice.summary.testing.hubMeeting
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SummaryHubBuilderTermStatesTest {
	private val classesOnThursday = hubAttempt(
		code = "CI5437",
		schedule = listOf(hubMeeting(day = HUB_THURSDAY, start = 3, end = 4))
	)

	private fun hub(
		attempts: List<AcademicAttempt> = listOf(classesOnThursday),
		evaluations: List<Evaluation> = emptyList(),
		hasSyncedEvaluations: Boolean = true,
		now: ScheduleNow = scheduleNow(),
		classesStartDate: String? = HUB_CLASSES_START
	) = buildSummaryHub(
		recordState = SummaryRecordState.Ready(hubRecord(hubTerm(attempts = attempts, classesStartDate = classesStartDate))),
		evaluations = ObservedSyncedSnapshot(value = evaluations, hasSynced = hasSyncedEvaluations),
		now = now,
		pensumProgress = null
	)

	// The week of a hub that has a term in progress, read as that kind.
	private val SummaryHub.progressWeek: SummaryWeek.InProgress?
		get() = week as? SummaryWeek.InProgress

	// A subject whose attempt was passed with the grade it carries, the way the record stores a graded attempt.
	private fun passedAttempt(code: String, credits: Int = 3) = hubAttempt(code = code).copy(
		credits = credits,
		academicScore = AttemptScore.numeric(4),
		academicOutcome = AttemptOutcome.APPROVED
	)

	/* The record with no term in progress: what it says of the terms it has */

	@Test
	fun withNoCurrentTerm_theWeekAndTheTermAreIdle_andTheCareerStays() {
		val progress = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)
		val historical = hubTerm(attempts = listOf(classesOnThursday), kind = TermKind.HISTORICAL)

		val result = buildSummaryHub(
			recordState = SummaryRecordState.Ready(hubRecord(historical)),
			evaluations = ObservedSyncedSnapshot(
				value = listOf(hubEvaluation(id = "late", code = "CI5437", date = hubDate(8))),
				hasSynced = true
			),
			now = scheduleNow(),
			pensumProgress = progress
		)

		assertTrue(result.attention.isEmpty())
		assertEquals(SummaryWeek.Idle(hasTerms = true), result.week)
		assertIs<SummaryTerm.Idle>(result.term)
		assertEquals(progress, result.career)
	}

	@Test
	fun withNoTermAtAll_aNewStudent_hasNoTermForTheWeekOrTheTermToSay() {
		val result = buildSummaryHub(
			recordState = SummaryRecordState.Ready(hubRecord()),
			evaluations = ObservedSyncedSnapshot(emptyList(), true),
			now = scheduleNow(),
			pensumProgress = null
		)

		assertEquals(SummaryWeek.Idle(hasTerms = false), result.week)
		assertEquals(SummaryTerm.Idle(hasTerms = false, finished = null), result.term)
	}

	@Test
	fun aNewStudentWithoutRecord_isTheFirstTermCase_andTheCareerStays() {
		val progress = PensumCreditsProgress(approvedCredits = 0, totalCredits = 240)

		val result = buildSummaryHub(
			recordState = SummaryRecordState.NewStudentWithoutRecord,
			evaluations = ObservedSyncedSnapshot(emptyList(), true),
			now = scheduleNow(),
			pensumProgress = progress
		)

		assertEquals(
			SummaryHub(
				attention = emptyList(),
				week = SummaryWeek.Idle(hasTerms = false),
				term = SummaryTerm.Idle(hasTerms = false, finished = null),
				career = progress,
				statistics = null
			),
			result.copy(statistics = null)
		)
		// Its statistics are empty, with the pensum as it was read: a new student has no closed term yet.
		val statistics = assertNotNull(result.statistics)
		assertTrue(statistics.closed.terms.isEmpty())
		assertEquals(progress, statistics.pensumProgress)
		assertNull(statistics.currentTerm)
	}

	@Test
	fun withOnlyPlannedTerms_theRecordHasTerms_butNoneIsFinished() {
		val planned = hubTerm(attempts = listOf(passedAttempt(code = "CI5437")), kind = TermKind.SYNTHETIC)

		val result = buildSummaryHub(
			recordState = SummaryRecordState.Ready(hubRecord(planned)),
			evaluations = ObservedSyncedSnapshot(emptyList(), true),
			now = scheduleNow(),
			pensumProgress = null
		)

		assertEquals(SummaryWeek.Idle(hasTerms = true), result.week)
		assertEquals(SummaryTerm.Idle(hasTerms = true, finished = null), result.term)
	}

	@Test
	fun theFinishedTerm_isTheHistoricalOne_withTheHighestOrder_andNotAPlannedOne() {
		val older = hubTerm(
			attempts = listOf(passedAttempt(code = "OLD1111")),
			kind = TermKind.HISTORICAL,
			periodCode = AcademicTermPeriod.APR_JUL
		).copy(id = "older", termOrder = 1)
		val newer = hubTerm(attempts = listOf(passedAttempt(code = "NEW1111")), kind = TermKind.HISTORICAL)
			.copy(id = "newer", termOrder = 2)
		val planned = hubTerm(attempts = listOf(hubAttempt(code = "PLN1111")), kind = TermKind.SYNTHETIC)
			.copy(id = "planned", termOrder = 3)

		val result = buildSummaryHub(
			recordState = SummaryRecordState.Ready(hubRecord(older, newer, planned)),
			evaluations = ObservedSyncedSnapshot(emptyList(), true),
			now = scheduleNow(),
			pensumProgress = null
		)

		val finished = assertIs<SummaryTerm.Idle>(result.term).finished
		assertNotNull(finished)
		assertEquals(AcademicTermPeriod.SEP_DEC, finished.periodCode)
		assertEquals(2026, finished.periodYear)
	}

	@Test
	fun theIndexOfTheFinishedTerm_isTheOfficialAverageDstPrinted() {
		val historical = hubTerm(
			attempts = listOf(passedAttempt(code = "CI5437")),
			kind = TermKind.HISTORICAL
		).copy(officialPeriodAverage = 14.5678)

		val result = buildSummaryHub(
			recordState = SummaryRecordState.Ready(hubRecord(historical)),
			evaluations = ObservedSyncedSnapshot(emptyList(), true),
			now = scheduleNow(),
			pensumProgress = null
		)
		val finished = assertIs<SummaryTerm.Idle>(result.term).finished

		assertEquals(14.5678, finished?.index)
	}

	@Test
	fun theApprovedCreditsOfTheFinishedTerm_areThoseOfItsApprovedAttempts_withoutTheWithdrawnOnes() {
		val historical = hubTerm(
			attempts = listOf(
				passedAttempt(code = "CI5437", credits = 4),
				passedAttempt(code = "CI4325", credits = 3).copy(
					academicScore = AttemptScore.numeric(2),
					academicOutcome = AttemptOutcome.FAILED
				),
				passedAttempt(code = "CI5311", credits = 2).copy(withdrawn = true)
			),
			kind = TermKind.HISTORICAL
		)

		val result = buildSummaryHub(
			recordState = SummaryRecordState.Ready(hubRecord(historical)),
			evaluations = ObservedSyncedSnapshot(emptyList(), true),
			now = scheduleNow(),
			pensumProgress = null
		)
		val finished = assertIs<SummaryTerm.Idle>(result.term).finished

		assertEquals(4, finished?.approvedCredits)
	}

	/* The week with nothing in it, and what the term says of its evaluations */

	@Test
	fun withNoEvaluationRegisteredInTheTerm_theWeekKnowsTheyAreSyncedAndNoneExists() {
		val week = hub(attempts = listOf(hubAttempt(code = "EG1114")), evaluations = emptyList()).progressWeek

		assertNotNull(week)
		assertTrue(week.hasSyncedEvaluations)
		assertFalse(week.hasEvaluations)
	}

	@Test
	fun withTheEvaluationsNotDownloaded_theWeekKnowsItDoesNotKnow() {
		val week = hub(
			attempts = listOf(hubAttempt(code = "EG1114")),
			evaluations = emptyList(),
			hasSyncedEvaluations = false
		).progressWeek

		assertNotNull(week)
		assertFalse(week.hasSyncedEvaluations)
		assertFalse(week.hasEvaluations)
	}

	@Test
	fun withEvaluationsButNoneThisWeek_theNextOneFromTomorrowOnIsNamed() {
		val evaluations = listOf(
			hubEvaluation(id = "later", code = "CI4325", date = hubDate(27)),
			hubEvaluation(id = "soon", code = "CI5437", date = hubDate(20)),
			hubEvaluation(id = "past", code = "CI5437", date = hubDate(14))
		)

		val week = hub(
			attempts = listOf(hubAttempt(code = "CI5437"), hubAttempt(code = "CI4325")),
			evaluations = evaluations
		).progressWeek

		assertNotNull(week)
		assertTrue(week.evaluations.isEmpty())
		assertTrue(week.hasEvaluations)
		val next = week.nextEvaluation
		assertNotNull(next)
		assertEquals("soon", next.evaluationId)
		assertEquals("CI5437", next.subjectCode)
		assertEquals(hubDate(20), next.date)
		assertFalse(next.isToday)
	}

	@Test
	fun theNextEvaluationOfTheWeek_isNotTodaysOne_butTomorrowsWhenThereIsOne() {
		val evaluations = listOf(
			hubEvaluation(id = "today", code = "CI5437", date = hubDate(15)),
			hubEvaluation(id = "tomorrow", code = "CI5437", date = hubDate(16))
		)

		val week = hub(attempts = listOf(hubAttempt(code = "CI5437")), evaluations = evaluations).progressWeek

		assertNotNull(week)
		assertEquals(listOf("today", "tomorrow"), week.evaluations.map { it.evaluationId })
		assertEquals("tomorrow", week.nextEvaluation?.evaluationId)
	}

	@Test
	fun aTieOnTheDayOfTheWeeksNextEvaluation_goesToTheSubjectCodeFirst() {
		val evaluations = listOf(
			hubEvaluation(id = "b", code = "CI5437", date = hubDate(22)),
			hubEvaluation(id = "a", code = "CI4325", date = hubDate(22))
		)

		val week = hub(
			attempts = listOf(hubAttempt(code = "CI5437"), hubAttempt(code = "CI4325")),
			evaluations = evaluations
		).progressWeek

		assertEquals("a", week?.nextEvaluation?.evaluationId)
	}

	@Test
	fun withEveryEvaluationGraded_thereIsNoNextEvaluation() {
		val evaluations = listOf(hubEvaluation(id = "graded", code = "CI5437", date = hubDate(22), grade = 14.0))

		val week = hub(attempts = listOf(hubAttempt(code = "CI5437")), evaluations = evaluations).progressWeek

		assertNull(week?.nextEvaluation)
	}
}
