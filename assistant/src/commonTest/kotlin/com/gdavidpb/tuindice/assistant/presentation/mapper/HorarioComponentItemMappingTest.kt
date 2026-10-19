package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.ScheduleBlock
import com.gdavidpb.tuindice.chatcontract.component.ScheduleClash
import com.gdavidpb.tuindice.chatcontract.component.ScheduleDay
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_next_in_progress_label
import tuindice.assistant.generated.resources.assistant_card_a_remaining_minutes
import tuindice.assistant.generated.resources.assistant_card_b_joined
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HorarioComponentItemMappingTest {
	@Test
	fun weekSchedule_leavesOutTheDaysWithoutAName_andTheDaysWithoutClasses() {
		val item = WeekScheduleComponent(
			id = "h1",
			fallbackText = "Semana",
			days = listOf(
				ScheduleDay(dayOfWeek = 0, blocks = listOf(ScheduleBlock("IS-101", "Programación I", 480, 600))),
				ScheduleDay(dayOfWeek = 2, blocks = emptyList()),
				ScheduleDay(dayOfWeek = 3, blocks = listOf(ScheduleBlock("IS-102", "Física", 480, 600, room = null)))
			)
		).toWeekScheduleItem()

		val day = item.days.single()
		assertEquals("", day.blocks.single().room)
		assertEquals(false, day.blocks.single().hasEvaluation)
	}

	@Test
	fun nextClass_countsWhatIsLeft_andShowsNothingWhileInProgressOrWhenOver() {
		val dayStart = 1_760_000_000_000L
		val component = NextClassComponent(
			id = "h2",
			fallbackText = "Próxima",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			dayOfWeek = 2,
			startMinute = 480,
			endMinute = 600,
			room = "  ",
			inProgress = false,
			dateMillis = dayStart
		)

		val later = component.toNextClassItem(nowMillis = dayStart + (480 - 30) * 60_000L)
		val inProgress = component.copy(inProgress = true).toNextClassItem(nowMillis = dayStart + 490 * 60_000L)
		val over = component.toNextClassItem(nowMillis = dayStart + 700 * 60_000L)

		assertEquals(UiText.Resource(Res.string.assistant_card_a_remaining_minutes, listOf(30L)), later.remainingText)
		assertNull(later.roomText)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_next_in_progress_label), inProgress.labelText)
		assertNull(inProgress.remainingText)
		assertNull(over.remainingText)
	}

	@Test
	fun clashes_joinTheCodesOfTheSubjectsThatMeet() {
		val item = ClashesComponent(
			id = "h3",
			fallbackText = "Choques",
			clashes = listOf(
				ScheduleClash(dayOfWeek = 2, startMinute = 480, endMinute = 600, subjectCodes = listOf("IS-101", "IS-102"))
			)
		).toClashesItem()

		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_joined, listOf(UiText.Raw("IS-101"), UiText.Raw("IS-102"))),
			item.rows.single().codes
		)
	}
}
