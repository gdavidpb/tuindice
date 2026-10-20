package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.mapper.toClockRangeText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.ScheduleBlock
import com.gdavidpb.tuindice.chatcontract.component.ScheduleClash
import com.gdavidpb.tuindice.chatcontract.component.ScheduleDay
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_clash_block_many
import tuindice.assistant.generated.resources.assistant_card_a_clash_block_one
import tuindice.assistant.generated.resources.assistant_card_a_next_caption_many
import tuindice.assistant.generated.resources.assistant_card_a_next_caption_one
import tuindice.assistant.generated.resources.assistant_card_a_next_in_progress_label
import tuindice.assistant.generated.resources.assistant_card_a_next_label
import tuindice.assistant.generated.resources.assistant_card_a_outcome_pending
import tuindice.assistant.generated.resources.assistant_card_a_remaining_minutes
import tuindice.assistant.generated.resources.assistant_card_a_see_all_clashes
import tuindice.assistant.generated.resources.assistant_card_b_joined
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class HorarioComponentItemMappingTest {
	@Test
	fun weekSchedule_leavesOutTheDaysWithoutAName_andTheDaysWithoutClasses() {
		val item = WeekScheduleComponent(
			id = "h1",
			fallbackText = "Semana",
			days = listOf(
				ScheduleDay(dayOfWeek = 0, blocks = listOf(block(room = "A-1"))),
				ScheduleDay(dayOfWeek = 2, blocks = emptyList()),
				ScheduleDay(dayOfWeek = 3, blocks = listOf(block(room = null)))
			)
		).toWeekScheduleItem()

		val day = item.days.single()
		assertEquals("", day.blocks.single().room)
		assertNull(day.blocks.single().evaluationChip)
	}

	@Test
	fun weekSchedule_readsTheBlocksFirst_andTheHoursAfterThem() {
		val row = WeekScheduleComponent(
			id = "h1",
			fallbackText = "Semana",
			days = listOf(ScheduleDay(dayOfWeek = 1, blocks = listOf(block(room = "A-1"))))
		).toWeekScheduleItem().days.single().blocks.single()

		assertEquals(
			classPlaceItem(startBlock = 1, endBlock = 2, classroom = "", clockRange = START_MINUTE..END_MINUTE).scheduleText,
			row.scheduleText
		)
		assertEquals((START_MINUTE..END_MINUTE).toClockRangeText(), row.hoursText)
	}

	@Test
	fun nextClass_countsWhatIsLeft_andIsInProgressWhileTheClockIsWithinItsHours() {
		val component = nextClass()

		val later = component.toNextClassItem(nowMillis = DAY_START_MILLIS + (START_MINUTE - 30) * MILLIS_PER_MINUTE)
		val inProgress = component.toNextClassItem(nowMillis = DAY_START_MILLIS + (START_MINUTE + 10) * MILLIS_PER_MINUTE)
		val over = component.toNextClassItem(nowMillis = DAY_START_MILLIS + (END_MINUTE + 60) * MILLIS_PER_MINUTE)

		assertEquals(UiText.Resource(Res.string.assistant_card_a_remaining_minutes, listOf(30L)), later.remainingText)
		assertNull(later.roomText)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_next_in_progress_label), inProgress.labelText)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_pending), CardChipTone.Current),
			inProgress.statusChip
		)
		assertNull(later.statusChip)
		assertNull(inProgress.remainingText)
		assertNull(over.remainingText)
	}

	@Test
	fun nextClass_isNotInProgressByTheReadingOfTheServer_whenTheClockSaysItIsOver() {
		val item = nextClass(serverSaysInProgress = true)
			.toNextClassItem(nowMillis = DAY_START_MILLIS + (END_MINUTE + 60) * MILLIS_PER_MINUTE)

		assertEquals(false, item.inProgress)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_next_label), item.labelText)
		assertNull(item.statusChip)
	}

	@Test
	fun nextClass_figureIsTheBlocks_andItsCaptionSaysTheHoursOfThem() {
		val item = nextClass(startBlock = 3, endBlock = 4, startMinute = 570, endMinute = 690)
			.toNextClassItem(nowMillis = DAY_START_MILLIS)

		assertEquals(UiText.Raw("3-4"), item.classFigure.value)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_a_next_caption_many, listOf((570..690).toClockRangeText())),
			item.classFigure.caption
		)
	}

	@Test
	fun nextClass_ofASingleBlock_figureIsTheNumberAndItsCaptionSaysBlock() {
		val item = nextClass(startBlock = 3, endBlock = 3, startMinute = 570, endMinute = 630)
			.toNextClassItem(nowMillis = DAY_START_MILLIS)

		assertEquals(UiText.Raw("3"), item.classFigure.value)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_a_next_caption_one, listOf((570..630).toClockRangeText())),
			item.classFigure.caption
		)
	}

	@Test
	fun clashes_joinTheCodesOfTheSubjectsThatMeet_andNameTheBlockOfTheClash() {
		val item = ClashesComponent(
			id = "h3",
			fallbackText = "Choques",
			clashes = listOf(
				ScheduleClash(
					dayOfWeek = 2,
					startMinute = 510,
					endMinute = 570,
					startBlock = 2,
					endBlock = 2,
					subjectCodes = listOf("IS-101", "IS-102")
				)
			)
		).toClashesItem()

		val row = item.rows.single()
		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_joined, listOf(UiText.Raw("IS-101"), UiText.Raw("IS-102"))),
			row.codes
		)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_see_all_clashes, listOf(1)), item.seeAllText)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_a_clash_block_one, listOf("2")),
			assertIs<UiText.Resource>(row.dayAndBlocksText).args.last()
		)
	}

	@Test
	fun clashes_ofSeveralBlocks_nameThemAsAPlural() {
		val row = ClashesComponent(
			id = "h4",
			fallbackText = "Choques",
			clashes = listOf(
				ScheduleClash(
					dayOfWeek = 3,
					startMinute = 570,
					endMinute = 690,
					startBlock = 3,
					endBlock = 4,
					subjectCodes = listOf("IS-101", "IS-102")
				)
			)
		).toClashesItem().rows.single()

		assertEquals(
			UiText.Resource(Res.string.assistant_card_a_clash_block_many, listOf("3-4")),
			assertIs<UiText.Resource>(row.dayAndBlocksText).args.last()
		)
	}

	private fun block(room: String? = "A-1"): ScheduleBlock =
		ScheduleBlock(
			subjectCode = "IS-101",
			subjectName = "Programación I",
			startMinute = START_MINUTE,
			endMinute = END_MINUTE,
			startBlock = 1,
			endBlock = 2,
			room = room
		)

	private fun nextClass(
		startBlock: Int = 1,
		endBlock: Int = 2,
		startMinute: Int = START_MINUTE,
		endMinute: Int = END_MINUTE,
		serverSaysInProgress: Boolean = false
	): NextClassComponent =
		NextClassComponent(
			id = "h2",
			fallbackText = "Próxima",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			dayOfWeek = 2,
			startMinute = startMinute,
			endMinute = endMinute,
			startBlock = startBlock,
			endBlock = endBlock,
			room = "  ",
			inProgress = serverSaysInProgress,
			dateMillis = DAY_START_MILLIS
		)

	private companion object {
		const val DAY_START_MILLIS = 1_760_000_000_000L
		const val MILLIS_PER_MINUTE = 60_000L

		// Block 1 to block 2: 7:30 to 9:30.
		const val START_MINUTE = 450
		const val END_MINUTE = 570
	}
}
