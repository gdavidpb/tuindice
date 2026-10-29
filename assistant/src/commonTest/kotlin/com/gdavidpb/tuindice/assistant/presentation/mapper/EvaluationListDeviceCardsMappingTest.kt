package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationWeekKey
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.component.WeekLoad
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationListDeviceCardsMappingTest {
	@Test
	fun aRowTheDeviceHas_isDrawnWithTheCardOfTheEvaluationsTab_andOneItLacksKeepsTheChatsValues() {
		val item = list().toEvaluationListItem(deviceCards = mapOf("ev-1" to TAB_CARD))

		assertEquals(TAB_CARD, item.rows[0].deviceCard)
		assertNull(item.rows[1].deviceCard)
		// The row the device lacks is drawn as before, from what the chat carries.
		assertEquals("Quiz 2", item.rows[1].title)
		assertEquals("IS-101", item.rows[1].code)
	}

	@Test
	fun withoutCards_everyRowKeepsTheChatsValues() {
		val item = list().toEvaluationListItem()

		assertEquals(listOf(null, null), item.rows.map { row -> row.deviceCard })
	}

	@Test
	fun theCardsOfTheTab_doNotMoveARowToAnotherWeek() {
		val withCards = list().toEvaluationListItem(weeks = WEEKS, deviceCards = mapOf("ev-1" to TAB_CARD))
		val withoutCards = list().toEvaluationListItem(weeks = WEEKS)

		assertEquals(withoutCards.weekKeys(), withCards.weekKeys())
		assertEquals(withoutCards.weeks, withCards.weeks)
		assertEquals(listOf(EvaluationWeekKey.Academic(2), EvaluationWeekKey.Continuous), withCards.weekKeys())
	}

	@Test
	fun theEvaluationsReadForAMessage_areTheOnesItsListsName_once() {
		val other = WeeklyLoadComponent(id = "w1", fallbackText = "Carga", weeks = listOf(WeekLoad(1, 1, 10.0, false)))

		assertEquals(setOf("ev-1", "ev-2"), listOf(list(), other, list()).listedEvaluationIds())
		assertEquals(emptySet(), listOf(other).listedEvaluationIds())
	}

	private fun EvaluationListItem.weekKeys() = rows.map { row -> row.weekKey }

	private fun list() = EvaluationListComponent(
		id = "e1",
		fallbackText = "Evaluaciones",
		items = listOf(
			EvaluationRow(
				evaluationId = "ev-1",
				subjectCode = "CI-2611",
				title = "Quiz 1",
				dateMillis = JANUARY_21_MILLIS,
				scheduleMode = "dated",
				grade = null,
				maxGrade = 20.0,
				state = "scheduled"
			),
			EvaluationRow(
				evaluationId = "ev-2",
				subjectCode = "IS-101",
				title = "Quiz 2",
				dateMillis = null,
				scheduleMode = "continuous",
				grade = null,
				maxGrade = 20.0,
				state = "pending"
			)
		)
	)

	private companion object {
		// The tab says «Sin nota» for the quiz the chat calls scheduled: its date has passed with no grade.
		val TAB_CARD = EvaluationCardItem(
			name = "QUIZ 1",
			subjectCode = "CI-2611",
			dateText = UiText.Raw("Miércoles 21/01/26"),
			highlightTone = EvaluationTone.Error,
			gradeText = "-- / 20",
			statusText = "Sin nota",
			statusTone = EvaluationTone.Error
		)

		val WEEK_ONE = LocalDate(2026, 1, 12)

		// Noon UTC on Wednesday 21 January 2026, in week 2 of a term that opens on Monday 12 January.
		const val JANUARY_21_MILLIS = 1_768_996_800_000L

		val WEEKS = EvaluationWeeksContext(
			calendar = object : AssistantAcademicCalendar {
				override val weekOneStart: LocalDate = WEEK_ONE
				override val weeks: IntRange = 1..12

				override fun weekOf(date: LocalDate): Int = WEEK_ONE.daysUntil(date) / 7 + 1
			},
			today = WEEK_ONE.plus(DatePeriod(days = 9))
		)
	}
}
