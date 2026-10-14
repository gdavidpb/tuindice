package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.testing.HUB_CLASSES_START
import com.gdavidpb.tuindice.summary.testing.HUB_FRIDAY
import com.gdavidpb.tuindice.summary.testing.HUB_MONDAY
import com.gdavidpb.tuindice.summary.testing.HUB_THURSDAY
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubDate
import com.gdavidpb.tuindice.summary.testing.hubEvaluation
import com.gdavidpb.tuindice.summary.testing.hubMeeting
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SummaryHubBuilderTest {
	private val today = hubDate(15)

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
		record = hubRecord(hubTerm(attempts = attempts, classesStartDate = classesStartDate)),
		isRecordReady = true,
		evaluations = ObservedSyncedSnapshot(value = evaluations, hasSynced = hasSyncedEvaluations),
		now = now,
		pensumProgress = null
	)

	/* The class of the week */

	@Test
	fun theClassInProgress_isTheFirstThingOfTheWeek() {
		val moment = hub().week?.classMoment

		assertNotNull(moment)
		assertTrue(moment.isInProgress)
		assertTrue(moment.isToday)
		assertEquals(today, moment.date)
		assertEquals("CI5437", moment.meeting.subjectCode)
	}

	@Test
	fun withNoClassInProgress_theNextOneOfTodayIsTheClass() {
		val moment = hub(now = scheduleNow(minuteOfDay = 7 * 60)).week?.classMoment

		assertNotNull(moment)
		assertEquals(false, moment.isInProgress)
		assertTrue(moment.isToday)
		assertEquals(today, moment.date)
	}

	@Test
	fun withNothingLeftToday_theNextClassIsOnItsOwnDay() {
		val mondayOnly = hubAttempt(
			code = "CI5311",
			schedule = listOf(hubMeeting(day = HUB_MONDAY, start = 1, end = 2, classroom = "MYS-116"))
		)

		val moment = hub(attempts = listOf(mondayOnly), now = scheduleNow(minuteOfDay = 12 * 60)).week?.classMoment

		assertNotNull(moment)
		assertEquals(false, moment.isInProgress)
		assertEquals(false, moment.isToday)
		assertEquals(hubDate(19), moment.date)
		assertEquals("CI5311", moment.meeting.subjectCode)
	}

	@Test
	fun withNoScheduleAndNoEvaluationsThisWeek_thereIsNoWeek() {
		assertNull(hub(attempts = listOf(hubAttempt(code = "EG1114"))).week)
	}

	@Test
	fun withNoScheduleButAnEvaluationThisWeek_theWeekHasNoClass() {
		val week = hub(
			attempts = listOf(hubAttempt(code = "EG1114")),
			evaluations = listOf(hubEvaluation(id = "e1", code = "EG1114", date = hubDate(16)))
		).week

		assertNotNull(week)
		assertNull(week.classMoment)
		assertEquals(listOf("e1"), week.evaluations.map { it.evaluationId })
	}

	@Test
	fun aWithdrawnSubject_hasNoClass() {
		val withdrawn = classesOnThursday.copy(withdrawn = true)

		assertNull(hub(attempts = listOf(withdrawn)).week)
	}

	@Test
	fun aSubjectGradedPassOrFail_isStillAClassInProgress() {
		val qualitative = classesOnThursday.copy(gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL)

		val week = hub(attempts = listOf(qualitative)).week

		val moment = week?.classMoment

		assertNotNull(moment)
		assertEquals("CI5437", moment.meeting.subjectCode)
		assertTrue(moment.isInProgress)
	}

	@Test
	fun aSubjectGradedPassOrFail_isTheNextClassWhenItIsTheOnlyOne() {
		val qualitative = hubAttempt(
			code = "CI5311",
			schedule = listOf(hubMeeting(day = HUB_MONDAY, start = 1, end = 2, classroom = "MYS-116")),
			gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL
		)

		val moment = hub(attempts = listOf(qualitative), now = scheduleNow(minuteOfDay = 12 * 60)).week?.classMoment

		assertNotNull(moment)
		assertEquals(false, moment.isInProgress)
		assertEquals("CI5311", moment.meeting.subjectCode)
	}

	@Test
	fun theWeek_saysWhetherTheTermHasAScheduleAtAll() {
		val onlyAnEvaluation = hub(
			attempts = listOf(hubAttempt(code = "EG1114")),
			evaluations = listOf(hubEvaluation(id = "e1", code = "EG1114", date = hubDate(16)))
		).week
		val withClasses = hub().week
		val onlyAQualitativeSubjectHasClasses = hub(
			attempts = listOf(
				hubAttempt(code = "EG1114"),
				classesOnThursday.copy(gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL)
			),
			evaluations = listOf(hubEvaluation(id = "e1", code = "EG1114", date = hubDate(16)))
		).week

		assertEquals(false, onlyAnEvaluation?.hasSchedule)
		assertEquals(true, withClasses?.hasSchedule)
		assertEquals(true, onlyAQualitativeSubjectHasClasses?.hasSchedule)
	}

	/* The evaluations of the week */

	@Test
	fun theEvaluationsOfTheWeek_areThoseFromTodayToSunday_inOrderOfDate() {
		val evaluations = listOf(
			hubEvaluation(id = "yesterday", code = "CI5437", date = hubDate(14)),
			hubEvaluation(id = "sunday", code = "CI5437", date = hubDate(18)),
			hubEvaluation(id = "tomorrow", code = "CI5437", date = hubDate(16)),
			hubEvaluation(id = "nextWeek", code = "CI5437", date = hubDate(19)),
			hubEvaluation(id = "today", code = "CI5437", date = today),
			hubEvaluation(id = "monday", code = "CI5437", date = hubDate(12))
		)

		val week = hub(evaluations = evaluations).week

		assertNotNull(week)
		assertEquals(listOf("today", "tomorrow", "sunday"), week.evaluations.map { it.evaluationId })
		assertEquals(listOf(true, false, false), week.evaluations.map { it.isToday })
	}

	@Test
	fun aContinuousEvaluation_hasNoDayAndIsLeftOut() {
		val continuous = hubEvaluation(id = "continuous", code = "CI5437", date = today)
			.copy(scheduleMode = EvaluationScheduleMode.CONTINUOUS)
		val undated = hubEvaluation(id = "undated", code = "CI5437", date = null)

		val week = hub(evaluations = listOf(continuous, undated)).week

		assertNotNull(week)
		assertTrue(week.evaluations.isEmpty())
	}

	@Test
	fun anEvaluation_inheritsTheClassOfItsSubjectThatDay() {
		val twoDays = hubAttempt(
			code = "CI5437",
			schedule = listOf(
				hubMeeting(day = HUB_THURSDAY, start = 3, end = 4),
				hubMeeting(day = HUB_FRIDAY, start = 5, classroom = "")
			)
		)
		val evaluations = listOf(
			hubEvaluation(id = "thursday", code = "CI5437", date = today),
			hubEvaluation(id = "friday", code = "CI5437", date = hubDate(16)),
			hubEvaluation(id = "saturday", code = "CI5437", date = hubDate(17))
		)

		val byId = hub(attempts = listOf(twoDays), evaluations = evaluations)
			.week!!.evaluations.associateBy { it.evaluationId }

		assertEquals(3, byId.getValue("thursday").meeting?.startBlock)
		assertEquals("MYS-210", byId.getValue("thursday").meeting?.classroom)
		assertEquals(5, byId.getValue("friday").meeting?.startBlock)
		assertEquals("", byId.getValue("friday").meeting?.classroom)
		assertNull(byId.getValue("saturday").meeting)
	}

	@Test
	fun theOrdinal_countsTheEvaluationsOfTheSameTypeOfTheSubject_whateverThisWeekFilters() {
		val evaluations = listOf(
			hubEvaluation(id = "second", code = "CI5437", date = hubDate(16)),
			hubEvaluation(id = "first", code = "CI5437", date = hubDate(2)),
			hubEvaluation(id = "essay", code = "CI5437", date = hubDate(17), type = EvaluationType.ESSAY)
		)

		val byId = hub(evaluations = evaluations).week!!.evaluations.associateBy { it.evaluationId }

		assertEquals(setOf("second", "essay"), byId.keys)
		assertEquals(2, byId.getValue("second").ordinal)
		assertEquals(1, byId.getValue("essay").ordinal)
	}

	@Test
	fun anOrphanEvaluation_isLeftOutOfEverything() {
		val orphan = hubEvaluation(id = "orphan", code = "XX0000", date = today)
		val overdueOrphan = hubEvaluation(id = "overdue-orphan", code = "XX0000", date = hubDate(1))

		val result = hub(evaluations = listOf(orphan, overdueOrphan))

		assertTrue(result.week!!.evaluations.isEmpty())
		assertTrue(result.attention.isEmpty())
	}

	/* The term */

	@Test
	fun theWeekOfTheTerm_isCountedFromTheDayClassesStarted() {
		assertEquals(3, hub().term?.academicWeek)
		assertEquals(1, hub(now = scheduleNow(date = hubDate(4), dayOfWeek = HUB_FRIDAY)).term?.academicWeek)
	}

	@Test
	fun withoutAStartDate_theWeekIsCountedFromThePeriodsFirstMonth() {
		assertEquals(7, hub(classesStartDate = null).term?.academicWeek)
	}

	@Test
	fun outsideTheTwelveWeeks_thereIsNoWeekToSay() {
		val afterTheTerm = hub(now = scheduleNow(date = LocalDate(2026, 12, 28), dayOfWeek = HUB_MONDAY))
		val beforeTheTerm = hub(now = scheduleNow(date = LocalDate(2026, 9, 20), dayOfWeek = 1))

		assertNull(afterTheTerm.term?.academicWeek)
		assertNull(beforeTheTerm.term?.academicWeek)
		assertEquals(AcademicTermPeriod.SEP_DEC, afterTheTerm.term?.periodCode)
	}

	@Test
	fun thePointsOfASubject_areThoseEarnedOverThoseEvaluated_ofTheGradedEvaluations() {
		val evaluations = listOf(
			hubEvaluation(id = "e1", code = "CI5437", date = hubDate(1), grade = 15.0),
			hubEvaluation(id = "e2", code = "CI5437", date = hubDate(8), grade = 3.0).copy(maxGrade = 5.0),
			hubEvaluation(id = "e3", code = "CI5437", date = hubDate(30)).copy(maxGrade = 30.0)
		)

		val points = hub(evaluations = evaluations).term!!.subjects.single().points

		assertNotNull(points)
		assertEquals(18.0, points.earned)
		assertEquals(25.0, points.evaluated)
	}

	@Test
	fun aSubjectWithNoGradedEvaluation_hasNoPoints() {
		val evaluations = listOf(hubEvaluation(id = "e1", code = "CI5437", date = hubDate(30)))

		val subject = hub(evaluations = evaluations).term!!.subjects.single()

		assertNull(subject.points)
	}

	@Test
	fun withTheEvaluationsNotDownloaded_noSubjectSaysItHasNoGrades() {
		val result = hub(evaluations = emptyList(), hasSyncedEvaluations = false)

		assertTrue(result.term!!.subjects.isEmpty())
		assertEquals(3, result.term.academicWeek)
	}

	@Test
	fun theSubjects_areOneRowEachByCode_leavingOutWithdrawnAndQualitativeOnes() {
		val attempts = listOf(
			hubAttempt(code = "MA1111"),
			hubAttempt(code = "CI5311"),
			hubAttempt(code = "EG1114", withdrawn = true),
			hubAttempt(code = "FS1111", gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL)
		)

		val subjects = hub(attempts = attempts).term!!.subjects

		assertEquals(listOf("CI5311", "MA1111"), subjects.map { it.subjectCode })
	}

	/* What needs attention */

	@Test
	fun withNothingToAttendTo_thereIsNoAttention() {
		val evaluations = listOf(
			hubEvaluation(id = "graded", code = "CI5437", date = hubDate(1), grade = 10.0),
			hubEvaluation(id = "future", code = "CI5437", date = hubDate(30))
		)

		assertTrue(hub(evaluations = evaluations).attention.isEmpty())
	}

	@Test
	fun oneOverdueEvaluation_isOneUngradedPoint_onTheWeekItFellIn() {
		val evaluations = listOf(hubEvaluation(id = "late", code = "CI5437", date = hubDate(8)))

		val attention = hub(evaluations = evaluations).attention

		assertEquals(
			listOf(
				SummaryAttention.UngradedEvaluations(
					count = 1,
					oldestWeekStart = hubDate(5),
					oldestEvaluationId = "late"
				)
			),
			attention
		)
	}

	@Test
	fun severalOverdueEvaluations_areOnePoint_onTheWeekOfTheOldest() {
		val evaluations = listOf(
			hubEvaluation(id = "recent", code = "CI5437", date = hubDate(14)),
			hubEvaluation(id = "oldest", code = "CI5437", date = hubDate(7)),
			hubEvaluation(id = "middle", code = "CI5437", date = hubDate(9)),
			hubEvaluation(id = "today", code = "CI5437", date = today),
			hubEvaluation(id = "graded", code = "CI5437", date = hubDate(1), grade = 12.0),
			hubEvaluation(id = "continuous", code = "CI5437", date = hubDate(1))
				.copy(scheduleMode = EvaluationScheduleMode.CONTINUOUS)
		)

		val attention = hub(evaluations = evaluations).attention

		assertEquals(
			listOf(
				SummaryAttention.UngradedEvaluations(
					count = 3,
					oldestWeekStart = hubDate(5),
					oldestEvaluationId = "oldest"
				)
			),
			attention
		)
	}

	@Test
	fun aTieOnTheDay_takesTheOldestBySubjectCode_andTheJumpLandsOnThatEvaluation() {
		val evaluations = listOf(
			hubEvaluation(id = "later-code", code = "CI5437", date = hubDate(7)),
			hubEvaluation(id = "earlier-code", code = "CI5311", date = hubDate(7))
		)
		val attention = hub(
			attempts = listOf(classesOnThursday, hubAttempt(code = "CI5311")),
			evaluations = evaluations
		).attention

		assertEquals(
			listOf(
				SummaryAttention.UngradedEvaluations(
					count = 2,
					oldestWeekStart = hubDate(5),
					oldestEvaluationId = "earlier-code"
				)
			),
			attention
		)
	}

	@Test
	fun aSubjectWithAnEnrollmentError_isAPoint_withWhatTheUniversityPrinted() {
		val clash = hubAttempt(code = "CI5311", enrollmentErrors = listOf("CHOQUE DE HORARIO", "OTRO"))

		val attention = hub(attempts = listOf(classesOnThursday, clash)).attention

		assertEquals(
			listOf(
				SummaryAttention.EnrollmentError(
					subjectCode = "CI5311",
					error = "CHOQUE DE HORARIO",
					attemptId = "attempt-CI5311"
				)
			),
			attention
		)
	}

	@Test
	fun blankErrorsAndWithdrawnSubjects_areNotPoints() {
		val blank = hubAttempt(code = "CI5311", enrollmentErrors = listOf("  ", ""))
		val withdrawn = hubAttempt(code = "MA1111", enrollmentErrors = listOf("CHOQUE"), withdrawn = true)

		assertTrue(hub(attempts = listOf(blank, withdrawn)).attention.isEmpty())
	}

	@Test
	fun theAttention_isOrdered_andHasThreeAtMost() {
		val attempts = listOf(
			hubAttempt(code = "MA1111", enrollmentErrors = listOf("CHOQUE DE HORARIO")),
			hubAttempt(code = "CI5311", enrollmentErrors = listOf("CHOQUE DE HORARIO")),
			hubAttempt(code = "EG1114", enrollmentErrors = listOf("SIN CUPO")),
			hubAttempt(code = "FS1111", enrollmentErrors = listOf("SIN CUPO"))
		)
		val evaluations = listOf(hubEvaluation(id = "late", code = "CI5311", date = hubDate(8)))

		val attention = hub(attempts = attempts, evaluations = evaluations).attention

		assertEquals(3, attention.size)
		assertTrue(attention[0] is SummaryAttention.UngradedEvaluations)
		assertEquals(
			listOf("CI5311", "EG1114"),
			attention.drop(1).map { (it as SummaryAttention.EnrollmentError).subjectCode }
		)
	}

	/* The career and the term that is not there */

	@Test
	fun theCareer_isThePensumProgress_orNullWithNoPensumInTheCache() {
		val progress = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)
		val record = hubRecord(hubTerm(attempts = listOf(classesOnThursday)))
		val evaluations = ObservedSyncedSnapshot(emptyList<Evaluation>(), true)

		assertEquals(progress, buildSummaryHub(record, true, evaluations, scheduleNow(), progress).career)
		assertNull(buildSummaryHub(record, true, evaluations, scheduleNow(), null).career)
	}

	@Test
	fun withNoCurrentTerm_onlyTheCareerRemains() {
		val progress = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)
		val historical = hubTerm(attempts = listOf(classesOnThursday), kind = TermKind.HISTORICAL)

		val result = buildSummaryHub(
			record = hubRecord(historical),
			isRecordReady = true,
			evaluations = ObservedSyncedSnapshot(
				value = listOf(hubEvaluation(id = "late", code = "CI5437", date = hubDate(8))),
				hasSynced = true
			),
			now = scheduleNow(),
			pensumProgress = progress
		)

		assertEquals(SummaryHub.Empty.copy(career = progress), result)
	}

	@Test
	fun withNoRecord_orARecordNotReady_onlyTheCareerRemains() {
		val progress = PensumCreditsProgress(approvedCredits = 10, totalCredits = 20)
		val record = hubRecord(hubTerm(attempts = listOf(classesOnThursday)))
		val evaluations = ObservedSyncedSnapshot(emptyList<Evaluation>(), true)

		val noRecord = buildSummaryHub(null, true, evaluations, scheduleNow(), progress)
		val notReady = buildSummaryHub(record, false, evaluations, scheduleNow(), progress)

		assertEquals(SummaryHub.Empty.copy(career = progress), noRecord)
		assertEquals(SummaryHub.Empty.copy(career = progress), notReady)
	}

	@Test
	fun withTwoCurrentTerms_theLaterOneIsTheTerm() {
		val old = hubTerm(
			attempts = listOf(hubAttempt(code = "OLD1111")),
			periodCode = AcademicTermPeriod.APR_JUL
		)
		val latest = hubTerm(attempts = listOf(hubAttempt(code = "NEW1111")))

		val result = buildSummaryHub(
			record = hubRecord(old, latest),
			isRecordReady = true,
			evaluations = ObservedSyncedSnapshot(emptyList(), true),
			now = scheduleNow(),
			pensumProgress = null
		)

		assertEquals(listOf("NEW1111"), result.term!!.subjects.map { it.subjectCode })
	}
}
