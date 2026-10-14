package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryNextEvaluation
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import com.gdavidpb.tuindice.summary.testing.HUB_CLASSES_START
import com.gdavidpb.tuindice.summary.testing.HUB_FRIDAY
import com.gdavidpb.tuindice.summary.testing.HUB_MONDAY
import com.gdavidpb.tuindice.summary.testing.HUB_THURSDAY
import com.gdavidpb.tuindice.summary.testing.HUB_WEDNESDAY
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
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

	// The week and the term of a hub that has a term in progress, read as that kind.
	private val SummaryHub.progressWeek: SummaryWeek.InProgress?
		get() = week as? SummaryWeek.InProgress

	private val SummaryHub.progressTerm: SummaryTerm.InProgress?
		get() = term as? SummaryTerm.InProgress

	/* The class of the week */

	@Test
	fun theClassInProgress_isTheFirstThingOfTheWeek() {
		val moment = hub().progressWeek?.classMoment

		assertNotNull(moment)
		assertTrue(moment.isInProgress)
		assertTrue(moment.isToday)
		assertEquals(today, moment.date)
		assertEquals("CI5437", moment.meeting.subjectCode)
	}

	@Test
	fun withNoClassInProgress_theNextOneOfTodayIsTheClass() {
		val moment = hub(now = scheduleNow(minuteOfDay = 7 * 60)).progressWeek?.classMoment

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

		val moment = hub(attempts = listOf(mondayOnly), now = scheduleNow(minuteOfDay = 12 * 60)).progressWeek?.classMoment

		assertNotNull(moment)
		assertEquals(false, moment.isInProgress)
		assertEquals(false, moment.isToday)
		assertEquals(hubDate(19), moment.date)
		assertEquals("CI5311", moment.meeting.subjectCode)
	}

	@Test
	fun withNoScheduleAndNoEvaluationsThisWeek_theWeekHasNoClassAndNoEvaluations() {
		val week = hub(attempts = listOf(hubAttempt(code = "EG1114"))).week

		assertIs<SummaryWeek.InProgress>(week)
		assertNull(week.classMoment)
		assertTrue(week.evaluations.isEmpty())
		assertFalse(week.hasSchedule)
		assertFalse(week.hasEvaluations)
	}

	@Test
	fun withNoScheduleButAnEvaluationThisWeek_theWeekHasNoClass() {
		val week = hub(
			attempts = listOf(hubAttempt(code = "EG1114")),
			evaluations = listOf(hubEvaluation(id = "e1", code = "EG1114", date = hubDate(16)))
		).progressWeek

		assertNotNull(week)
		assertNull(week.classMoment)
		assertEquals(listOf("e1"), week.evaluations.map { it.evaluationId })
	}

	@Test
	fun aWithdrawnSubject_hasNoClass() {
		val withdrawn = classesOnThursday.copy(withdrawn = true)
		val week = hub(attempts = listOf(withdrawn)).week

		assertIs<SummaryWeek.InProgress>(week)
		assertNull(week.classMoment)
		assertFalse(week.hasSchedule)
	}

	@Test
	fun aSubjectGradedPassOrFail_isStillAClassInProgress() {
		val qualitative = classesOnThursday.copy(gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL)

		val week = hub(attempts = listOf(qualitative)).progressWeek

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

		val moment = hub(attempts = listOf(qualitative), now = scheduleNow(minuteOfDay = 12 * 60)).progressWeek?.classMoment

		assertNotNull(moment)
		assertEquals(false, moment.isInProgress)
		assertEquals("CI5311", moment.meeting.subjectCode)
	}

	@Test
	fun theWeek_saysWhetherTheTermHasAScheduleAtAll() {
		val onlyAnEvaluation = hub(
			attempts = listOf(hubAttempt(code = "EG1114")),
			evaluations = listOf(hubEvaluation(id = "e1", code = "EG1114", date = hubDate(16)))
		).progressWeek
		val withClasses = hub().progressWeek
		val onlyAQualitativeSubjectHasClasses = hub(
			attempts = listOf(
				hubAttempt(code = "EG1114"),
				classesOnThursday.copy(gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL)
			),
			evaluations = listOf(hubEvaluation(id = "e1", code = "EG1114", date = hubDate(16)))
		).progressWeek

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

		val week = hub(evaluations = evaluations).progressWeek

		assertNotNull(week)
		assertEquals(listOf("today", "tomorrow", "sunday"), week.evaluations.map { it.evaluationId })
		assertEquals(listOf(true, false, false), week.evaluations.map { it.isToday })
	}

	@Test
	fun aContinuousEvaluation_hasNoDayAndIsLeftOut() {
		val continuous = hubEvaluation(id = "continuous", code = "CI5437", date = today)
			.copy(scheduleMode = EvaluationScheduleMode.CONTINUOUS)
		val undated = hubEvaluation(id = "undated", code = "CI5437", date = null)

		val week = hub(evaluations = listOf(continuous, undated)).progressWeek

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
			.progressWeek!!.evaluations.associateBy { it.evaluationId }

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

		val byId = hub(evaluations = evaluations).progressWeek!!.evaluations.associateBy { it.evaluationId }

		assertEquals(setOf("second", "essay"), byId.keys)
		assertEquals(2, byId.getValue("second").ordinal)
		assertEquals(1, byId.getValue("essay").ordinal)
	}

	@Test
	fun anOrphanEvaluation_isLeftOutOfEverything() {
		val orphan = hubEvaluation(id = "orphan", code = "XX0000", date = today)
		val overdueOrphan = hubEvaluation(id = "overdue-orphan", code = "XX0000", date = hubDate(1))

		val result = hub(evaluations = listOf(orphan, overdueOrphan))

		assertTrue(result.progressWeek!!.evaluations.isEmpty())
		assertTrue(result.attention.isEmpty())
	}

	/* The term */

	@Test
	fun theWeekOfTheTerm_isCountedFromTheDayClassesStarted() {
		assertEquals(3, hub().progressTerm?.academicWeek)
		assertEquals(1, hub(now = scheduleNow(date = hubDate(4), dayOfWeek = HUB_FRIDAY)).progressTerm?.academicWeek)
	}

	@Test
	fun withoutAStartDate_theWeekIsCountedFromThePeriodsFirstMonth() {
		assertEquals(7, hub(classesStartDate = null).progressTerm?.academicWeek)
	}

	@Test
	fun outsideTheTwelveWeeks_thereIsNoWeekToSay() {
		val afterTheTerm = hub(now = scheduleNow(date = LocalDate(2026, 12, 28), dayOfWeek = HUB_MONDAY))
		val beforeTheTerm = hub(now = scheduleNow(date = LocalDate(2026, 9, 20), dayOfWeek = 1))

		assertNull(afterTheTerm.progressTerm?.academicWeek)
		assertNull(beforeTheTerm.progressTerm?.academicWeek)
		assertEquals(AcademicTermPeriod.SEP_DEC, afterTheTerm.progressTerm?.periodCode)
	}

	@Test
	fun thePointsOfASubject_areThoseEarnedOverThoseEvaluated_ofTheGradedEvaluations() {
		val evaluations = listOf(
			hubEvaluation(id = "e1", code = "CI5437", date = hubDate(1), grade = 15.0),
			hubEvaluation(id = "e2", code = "CI5437", date = hubDate(8), grade = 3.0).copy(maxGrade = 5.0),
			hubEvaluation(id = "e3", code = "CI5437", date = hubDate(30)).copy(maxGrade = 30.0)
		)

		val points = hub(evaluations = evaluations).progressTerm!!.subjects.single().points

		assertNotNull(points)
		assertEquals(18.0, points.earned)
		assertEquals(25.0, points.evaluated)
	}

	@Test
	fun aSubjectWithEvaluationsButNoGrade_hasPointsWithNoneGraded() {
		val evaluations = listOf(hubEvaluation(id = "e1", code = "CI5437", date = hubDate(30)))

		val points = hub(evaluations = evaluations).progressTerm!!.subjects.single().points

		assertNotNull(points)
		assertEquals(0, points.gradedCount)
		assertEquals(1, points.totalCount)
	}

	@Test
	fun aSubjectWithNoEvaluationRegistered_hasNoPoints() {
		val subject = hub(attempts = listOf(classesOnThursday)).progressTerm!!.subjects.single()

		assertNull(subject.points)
	}

	@Test
	fun withTheEvaluationsNotDownloaded_noSubjectSaysItHasNoGrades() {
		val result = hub(evaluations = emptyList(), hasSyncedEvaluations = false)

		assertTrue(result.progressTerm!!.subjects.isEmpty())
		assertEquals(3, result.progressTerm!!.academicWeek)
	}

	@Test
	fun theSubjects_areOneRowEachByCode_leavingOutWithdrawnAndQualitativeOnes() {
		val attempts = listOf(
			hubAttempt(code = "MA1111"),
			hubAttempt(code = "CI5311"),
			hubAttempt(code = "EG1114", withdrawn = true),
			hubAttempt(code = "FS1111", gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL)
		)

		val subjects = hub(attempts = attempts).progressTerm!!.subjects

		assertEquals(listOf("CI5311", "MA1111"), subjects.map { it.subjectCode })
	}

	@Test
	fun theCredits_areThoseOfTheRows_andTheTermAddsThemUp() {
		val attempts = listOf(
			hubAttempt(code = "EG1114"),
			hubAttempt(code = "CI5437").copy(credits = 4),
			hubAttempt(code = "CI5311").copy(credits = 4),
			hubAttempt(code = "MA1111", withdrawn = true).copy(credits = 9)
		)

		val term = hub(attempts = attempts).progressTerm!!

		assertEquals(listOf(4, 4, 3), term.subjects.map { it.credits })
		assertEquals(11, term.credits)
	}

	@Test
	fun theWeeksLeft_areTheWeeksAfterTheOneTheTermIsIn_andZeroInTheTwelfth() {
		fun weeksLeftOn(date: LocalDate, dayOfWeek: Int) =
			hub(now = scheduleNow(date = date, dayOfWeek = dayOfWeek)).progressTerm?.weeksLeft

		assertEquals(9, weeksLeftOn(today, HUB_THURSDAY))
		assertEquals(1, weeksLeftOn(LocalDate(2026, 12, 9), HUB_WEDNESDAY))
		assertEquals(0, weeksLeftOn(LocalDate(2026, 12, 16), HUB_WEDNESDAY))
	}

	@Test
	fun outsideTheTwelveWeeks_thereAreNoWeeksLeftToSay() {
		val afterTheTerm = hub(now = scheduleNow(date = LocalDate(2026, 12, 28), dayOfWeek = HUB_MONDAY))
		val beforeTheTerm = hub(now = scheduleNow(date = LocalDate(2026, 9, 20), dayOfWeek = 1))

		assertNull(afterTheTerm.progressTerm?.weeksLeft)
		assertNull(beforeTheTerm.progressTerm?.weeksLeft)
	}

	@Test
	fun theEvaluatedOfASubject_isTheMaxGradesOfItsGradedOnes_andItStopsAtOneHundred() {
		// The fixtures give every evaluation a maximum of 20: six graded ones would make 120.
		val graded = (1..6).map { day ->
			hubEvaluation(id = "g$day", code = "CI5437", date = hubDate(day), grade = 10.0)
		}
		val ungraded = hubEvaluation(id = "u", code = "CI5437", date = hubDate(30))

		fun evaluatedOf(evaluations: List<Evaluation>) =
			hub(evaluations = evaluations).progressTerm!!.subjects.single().evaluatedOfTotal

		assertEquals(60.0, evaluatedOf(graded.take(3) + ungraded))
		assertEquals(100.0, evaluatedOf(graded))
		assertEquals(0.0, evaluatedOf(listOf(ungraded)))
	}

	@Test
	fun theNextEvaluation_isTheEarliestUngradedOneThatHasNotGoneBy_andTodayCounts() {
		val evaluations = listOf(
			hubEvaluation(id = "overdue", code = "CI5437", date = hubDate(14)),
			hubEvaluation(id = "graded", code = "CI5437", date = hubDate(16), grade = 12.0),
			hubEvaluation(id = "later", code = "CI5437", date = hubDate(26)),
			hubEvaluation(id = "today", code = "CI5437", date = today),
			hubEvaluation(id = "continuous", code = "CI5437", date = hubDate(20))
				.copy(scheduleMode = EvaluationScheduleMode.CONTINUOUS)
		)

		val next = hub(evaluations = evaluations).progressTerm!!.subjects.single().nextEvaluation

		assertEquals("today", next?.evaluationId)
		assertEquals(true, next?.isToday)
	}

	@Test
	fun theNextEvaluation_namesItsTypeAndNumber_andCarriesItsDay() {
		val evaluations = listOf(
			hubEvaluation(id = "test-1", code = "CI5437", date = hubDate(2), grade = 9.0),
			hubEvaluation(id = "test-2", code = "CI5437", date = hubDate(26)),
			hubEvaluation(id = "essay", code = "CI5437", date = hubDate(27), type = EvaluationType.ESSAY)
		)

		val next = hub(evaluations = evaluations).progressTerm!!.subjects.single().nextEvaluation

		assertEquals(
			SummaryNextEvaluation(
				evaluationId = "test-2",
				subjectCode = "CI5437",
				type = EvaluationType.TEST,
				ordinal = 2,
				date = hubDate(26),
				isToday = false
			),
			next
		)
	}

	@Test
	fun aTieOnTheDayOfTheNextEvaluation_goesToItsNumberThenItsId_whateverTheOrderGiven() {
		fun nextOf(evaluations: List<Evaluation>) =
			hub(evaluations = evaluations).progressTerm!!.subjects.single().nextEvaluation?.evaluationId

		// The essay is the first of its day by number (1 against the second test's 2), whatever its id.
		val byNumber = listOf(
			hubEvaluation(id = "test-1", code = "CI5437", date = hubDate(2), grade = 9.0),
			hubEvaluation(id = "test-2", code = "CI5437", date = hubDate(26)),
			hubEvaluation(id = "zeta", code = "CI5437", date = hubDate(26), type = EvaluationType.ESSAY)
		)
		// Both are the first of their type on the same day: the id decides, the same in either order.
		val byId = listOf(
			hubEvaluation(id = "test-b", code = "CI5437", date = hubDate(26)),
			hubEvaluation(id = "essay-a", code = "CI5437", date = hubDate(26), type = EvaluationType.ESSAY)
		)

		assertEquals("zeta", nextOf(byNumber))
		assertEquals("essay-a", nextOf(byId))
		assertEquals("essay-a", nextOf(byId.reversed()))
	}

	@Test
	fun withNothingLeftToGrade_thereIsNoNextEvaluation() {
		val evaluations = listOf(
			hubEvaluation(id = "late", code = "CI5437", date = hubDate(8)),
			hubEvaluation(id = "graded", code = "CI5437", date = hubDate(30), grade = 10.0)
		)

		assertNull(hub(evaluations = evaluations).progressTerm!!.subjects.single().nextEvaluation)
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

		assertEquals(listOf("NEW1111"), result.progressTerm!!.subjects.map { it.subjectCode })
	}
}
