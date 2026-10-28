package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationWeekKey
import com.gdavidpb.tuindice.assistant.presentation.testing.TermCalendarFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.millisOf
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationWeeksMappingTest {
	// Week 1 of the term opens on Monday 12 January 2026, so week 2 opens on 19 January and week 3 on 26 January.
	private val calendar = TermCalendarFixture(weekOneStart = LocalDate(2026, 1, 12))

	@Test
	fun when_rowsAreListed_then_eachIsFiledUnderTheWeekOfItsDate_andAnUndatedOneIsContinuous() {
		val item = listOfRows(
			"Parcial 1" to LocalDate(2026, 1, 13),
			"Parcial 2" to LocalDate(2026, 1, 21),
			"Quiz" to null
		).toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 1, 13)))

		assertEquals(
			listOf(EvaluationWeekKey.Academic(1), EvaluationWeekKey.Academic(2), EvaluationWeekKey.Continuous),
			item.rows.map { row -> row.weekKey }
		)
	}

	@Test
	fun when_todayHasEvaluations_then_theSheetOpensOnTheWeekOfToday() {
		val item = listOfRows(
			"Parcial 1" to LocalDate(2026, 1, 13),
			"Parcial 2" to LocalDate(2026, 1, 21)
		).toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 1, 21)))

		assertEquals(EvaluationWeekKey.Academic(2), item.weeks?.initialKey)
	}

	@Test
	fun when_todayHasNoEvaluationsAndNoLaterWeekHasAny_then_theSheetOpensOnTheLatestWeekBeforeToday() {
		val item = listOfRows(
			"Parcial 1" to LocalDate(2026, 1, 27),
			"Parcial 2" to LocalDate(2026, 2, 10)
		).toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 2, 18)))

		assertEquals(EvaluationWeekKey.Academic(5), item.weeks?.initialKey)
	}

	@Test
	fun when_todayHasNoEvaluations_then_theSheetOpensOnTheNearestWeekAfterToday() {
		// Today is in week 5, which lists nothing: week 4 is behind it, so the nearest week that lists some is week 6.
		val item = listOfRows(
			"Parcial 1" to LocalDate(2026, 2, 4),
			"Parcial 2" to LocalDate(2026, 2, 20)
		).toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 2, 10)))

		assertEquals(EvaluationWeekKey.Academic(6), item.weeks?.initialKey)
	}

	@Test
	fun when_theTermHasNotStarted_then_theSheetOpensOnTheFirstWeekThatHasRows() {
		val item = listOfRows(
			"Parcial 1" to LocalDate(2026, 1, 20),
			"Parcial 2" to LocalDate(2026, 2, 10)
		).toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 1, 1)))

		assertEquals(EvaluationWeekKey.Academic(2), item.weeks?.initialKey)
	}

	@Test
	fun when_theTermIsOver_then_theSheetOpensOnTheLatestWeekThatHasRows() {
		val item = listOfRows(
			"Parcial 1" to LocalDate(2026, 1, 20),
			"Parcial 2" to LocalDate(2026, 2, 10)
		).toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 5, 1)))

		assertEquals(EvaluationWeekKey.Academic(5), item.weeks?.initialKey)
	}

	@Test
	fun when_onlyUndatedRowsAreListed_then_theSheetOpensOnTheContinuousWeek() {
		val item = listOfRows("Quiz" to null)
			.toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 2, 18)))

		assertEquals(EvaluationWeekKey.Continuous, item.weeks?.initialKey)
	}

	@Test
	fun when_aDatedRowExists_then_theSheetDoesNotOpenOnTheContinuousWeek() {
		val item = listOfRows("Parcial 1" to LocalDate(2026, 2, 10), "Quiz" to null)
			.toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 2, 18)))

		assertEquals(EvaluationWeekKey.Academic(5), item.weeks?.initialKey)
	}

	@Test
	fun when_noRowIsListed_then_theSheetOpensOnTheWeekOfToday() {
		val item = listOfRows()
			.toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 2, 18)))

		assertEquals(EvaluationWeekKey.Academic(6), item.weeks?.initialKey)
	}

	@Test
	fun when_aRowHasNoWeek_then_theContinuousWeekComesFirst_andTheStripHoldsEveryWeekOfTheTerm() {
		val withUndated = listOfRows("Parcial 1" to LocalDate(2026, 1, 13), "Quiz" to null)
			.toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 1, 13)))
		val allDated = listOfRows("Parcial 1" to LocalDate(2026, 1, 13))
			.toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 1, 13)))

		val undatedStrip = assertNotNull(withUndated.weeks).strip
		val datedStrip = assertNotNull(allDated.weeks).strip

		assertEquals(EvaluationWeekKey.Continuous, undatedStrip.first().key)
		assertEquals(13, undatedStrip.size)
		assertEquals(EvaluationWeekKey.Academic(1), datedStrip.first().key)
		assertEquals(12, datedStrip.size)
	}

	@Test
	fun when_aWeekIsShown_then_itsDaysRunFromItsMonday_andMarkTodayAndTheDaysWithEvaluations() {
		val item = listOfRows("Parcial 1" to LocalDate(2026, 1, 21))
			.toEvaluationListItem(EvaluationWeeksContext(calendar = calendar, today = LocalDate(2026, 1, 21)))

		val week = assertNotNull(item.weeks).strip.first { week -> week.key == EvaluationWeekKey.Academic(2) }

		assertEquals(listOf("19", "20", "21", "22", "23", "24", "25"), week.days.map { day -> day.dayText })
		assertEquals(listOf(false, false, true, false, false, false, false), week.days.map { day -> day.isSelected })
		assertEquals(listOf(false, false, true, false, false, false, false), week.days.map { day -> day.hasEvaluations })
		assertTrue(week.isCurrent)
	}

	@Test
	fun when_theAppHasNoCurrentTerm_then_theRowsHaveNoWeek_andTheSheetHasNoWeeks() {
		val item = listOfRows("Parcial 1" to LocalDate(2026, 1, 13), "Quiz" to null).toEvaluationListItem()

		assertNull(item.weeks)
		assertEquals(listOf<EvaluationWeekKey?>(null, null), item.rows.map { row -> row.weekKey })
	}

	// The rows of a list, each with the title and the date given. A row without a date is undated, so it is continuous.
	private fun listOfRows(vararg rows: Pair<String, LocalDate?>): EvaluationListComponent {
		return EvaluationListComponent(
			id = "list",
			fallbackText = "Evaluaciones",
			items = rows.map { (title, date) ->
				EvaluationRow(
					evaluationId = title,
					subjectCode = "IS-101",
					title = title,
					dateMillis = date?.let(::millisOf),
					scheduleMode = "dated",
					grade = null,
					maxGrade = 20.0,
					state = "pending"
				)
			}
		)
	}
}
