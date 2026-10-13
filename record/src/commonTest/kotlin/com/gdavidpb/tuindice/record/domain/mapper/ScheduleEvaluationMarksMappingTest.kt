package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.EvaluationTestZone
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.clashingWeekAttempts
import com.gdavidpb.tuindice.record.testing.scheduleAttempt
import com.gdavidpb.tuindice.record.testing.scheduleEntry
import com.gdavidpb.tuindice.record.testing.scheduleEvaluation
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The week of ScheduleTestDate: Monday 2026-10-05 to Sunday 2026-10-11. In the attempts of
// clashingWeekAttempts a1 meets Monday (blocks 1-2) and Wednesday (block 3), a2 meets Monday (2-3).
class ScheduleEvaluationMarksMappingTest {
	private val monday = LocalDate(2026, 10, 5)
	private val wednesday = LocalDate(2026, 10, 7)
	private val sunday = LocalDate(2026, 10, 11)

	@Test
	fun anEvaluationOfTheWeekOnAClassDay_marksTheMeetingItInherits() {
		val marks = marksOf(scheduleEvaluation(attemptId = "a1", date = wednesday))

		assertEquals(monday, marks.weekStart)
		assertEquals(
			setOf(Triple("a1", ScheduleDay.Wednesday.code, 3)),
			marks.meetings.map { Triple(it.attemptId, it.dayOfWeek, it.startBlock) }.toSet()
		)
	}

	@Test
	fun theMondayAndTheSundayAreTheEdgesOfTheWeek() {
		val attempts = listOf(
			scheduleAttempt(
				id = "a1",
				code = "CI5311",
				schedule = listOf(
					scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2),
					scheduleEntry(day = ScheduleDay.Sunday, blocks = 4..5)
				)
			)
		)

		val marks = marksIn(
			attempts,
			scheduleEvaluation(attemptId = "a1", date = monday),
			scheduleEvaluation(attemptId = "a1", date = sunday)
		)

		assertEquals(
			setOf(ScheduleDay.Monday.code, ScheduleDay.Sunday.code),
			marks.meetings.map { it.dayOfWeek }.toSet()
		)
	}

	@Test
	fun theDaysJustOutsideTheWeek_markNothing() {
		val attempts = listOf(
			scheduleAttempt(
				id = "a1",
				code = "CI5311",
				schedule = listOf(
					scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2),
					scheduleEntry(day = ScheduleDay.Sunday, blocks = 4..5)
				)
			)
		)

		// The Sunday before the week and the Monday after it, both class days of the subject.
		val marks = marksIn(
			attempts,
			scheduleEvaluation(attemptId = "a1", date = monday.plus(DatePeriod(days = -1))),
			scheduleEvaluation(attemptId = "a1", date = sunday.plus(DatePeriod(days = 1)))
		)

		assertTrue(marks.meetings.isEmpty())
	}

	@Test
	fun theWeekIsTheOneOfToday_wherever_todayFallsInIt() {
		val attempts = clashingWeekAttempts()
		val evaluation = scheduleEvaluation(attemptId = "a1", date = wednesday)

		listOf(monday, wednesday, sunday).forEach { today ->
			val marks = attempts.evaluationMarksOn(
				evaluations = listOf(evaluation),
				today = today,
				timeZone = EvaluationTestZone
			)

			assertEquals(monday, marks.weekStart, "today: $today")
			assertEquals(1, marks.meetings.size, "today: $today")
		}

		val nextMonday = attempts.evaluationMarksOn(
			evaluations = listOf(evaluation),
			today = sunday.plus(DatePeriod(days = 1)),
			timeZone = EvaluationTestZone
		)

		assertEquals(sunday.plus(DatePeriod(days = 1)), nextMonday.weekStart)
		assertTrue(nextMonday.meetings.isEmpty())
	}

	@Test
	fun aContinuousEvaluation_andOneWithoutADate_markNothing() {
		val marks = marksOf(
			scheduleEvaluation(
				attemptId = "a1",
				date = wednesday,
				scheduleMode = EvaluationScheduleMode.CONTINUOUS,
				id = "continuous"
			),
			scheduleEvaluation(attemptId = "a1", date = null, id = "undated")
		)

		assertTrue(marks.meetings.isEmpty())
	}

	@Test
	fun anEvaluationOnADayTheSubjectHasNoClass_marksNothing() {
		// a1 has class Monday and Wednesday; this is a Thursday.
		val marks = marksOf(scheduleEvaluation(attemptId = "a1", date = LocalDate(2026, 10, 8)))

		assertTrue(marks.meetings.isEmpty())
	}

	@Test
	fun anEvaluationOfASubjectWithoutSchedule_marksNothing() {
		val marks = marksOf(scheduleEvaluation(attemptId = "a3", date = wednesday))

		assertTrue(marks.meetings.isEmpty())
	}

	@Test
	fun twoEvaluationsOnTheSameMeeting_leaveOneMark() {
		val marks = marksOf(
			scheduleEvaluation(attemptId = "a1", date = wednesday, id = "first"),
			scheduleEvaluation(attemptId = "a1", date = wednesday, id = "second")
		)

		assertEquals(1, marks.meetings.size)
	}

	@Test
	fun twoEvaluationsOnTwoMeetingsOfTheSameClash_markBoth() {
		// a1 and a2 both meet Monday: each evaluation marks the meeting of its own subject.
		val marks = marksOf(
			scheduleEvaluation(attemptId = "a1", date = monday),
			scheduleEvaluation(attemptId = "a2", date = monday)
		)

		assertEquals(setOf("a1", "a2"), marks.meetings.map { it.attemptId }.toSet())
	}

	@Test
	fun anEvaluationOnTheDayOfASubjectWithTwoMeetings_marksTheFirstOnly() {
		val attempts = listOf(
			scheduleAttempt(
				id = "a1",
				code = "CI5311",
				schedule = listOf(
					scheduleEntry(day = ScheduleDay.Monday, blocks = 5..6),
					scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2)
				)
			)
		)

		val marks = marksIn(attempts, scheduleEvaluation(attemptId = "a1", date = monday))

		assertEquals(listOf(1), marks.meetings.map { it.startBlock })
	}

	@Test
	fun anEvaluationOfAWithdrawnOrUnknownAttempt_isLeftOut() {
		val withdrawn = scheduleAttempt(
			id = "gone",
			code = "CI0000",
			schedule = listOf(scheduleEntry(day = ScheduleDay.Wednesday, blocks = 1..2))
		).copy(withdrawn = true)

		val marks = marksIn(
			clashingWeekAttempts() + withdrawn,
			scheduleEvaluation(attemptId = "gone", date = wednesday),
			scheduleEvaluation(attemptId = "nobody", date = wednesday)
		)

		assertTrue(marks.meetings.isEmpty())
	}

	@Test
	fun noEvaluationsAtAll_marksNothing_butStillSaysWhichWeek() {
		val marks = marksOf()

		assertEquals(monday, marks.weekStart)
		assertTrue(marks.meetings.isEmpty())
	}

	@Test
	fun theWeekOfTheTestDateIsTheOneThisFileAssumes() {
		assertEquals(monday, ScheduleTestDate)
	}

	private fun marksOf(vararg evaluations: Evaluation) = marksIn(clashingWeekAttempts(), *evaluations)

	private fun marksIn(attempts: List<AttemptProjection>, vararg evaluations: Evaluation) =
		attempts.evaluationMarksOn(
			evaluations = evaluations.toList(),
			today = ScheduleTestDate,
			timeZone = EvaluationTestZone
		)
}
