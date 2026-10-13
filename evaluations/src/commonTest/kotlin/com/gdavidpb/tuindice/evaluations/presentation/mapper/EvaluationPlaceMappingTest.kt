package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_place_block
import tuindice.evaluations.generated.resources.evaluation_place_blocks
import tuindice.evaluations.generated.resources.evaluation_place_in_class
import tuindice.evaluations.generated.resources.evaluation_place_with_room
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationPlaceMappingTest {
	private companion object {
		const val SUNDAY = 1
		const val MONDAY = 2
		const val SATURDAY = 7

		// 2026-10-12 is a Monday.
		val MONDAY_DATE = LocalDate(2026, 10, 12)
		val TUESDAY_DATE = LocalDate(2026, 10, 13)
	}

	private fun meeting(day: Int, start: Int, end: Int = start, classroom: String = "MYS-116") = ScheduleMeeting(
		attemptId = DEFAULT_EVALUATION_SUBJECT.id,
		subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
		section = 1,
		dayOfWeek = day,
		startBlock = start,
		endBlock = end,
		classroom = classroom
	)

	@Test
	fun toEvaluationPlaceItem_ofSeveralBlocks_saysTheRangeTheRoomAndTheHours() {
		val place = meeting(day = MONDAY, start = 1, end = 2).toEvaluationPlaceItem()

		assertEquals(
			UiText.Resource(
				Res.string.evaluation_place_with_room,
				listOf(UiText.Resource(Res.string.evaluation_place_blocks, listOf(1, 2)), "MYS-116")
			),
			place.placeText
		)
		// Block 1 starts at 7:30 and every block lasts an hour: the last block ends at 9:30.
		assertEquals("7:30–9:30", place.timeText)
	}

	@Test
	fun toEvaluationPlaceItem_ofASingleBlock_saysBlockInTheSingular() {
		val place = meeting(day = MONDAY, start = 3).toEvaluationPlaceItem()

		assertEquals(
			UiText.Resource(
				Res.string.evaluation_place_with_room,
				listOf(UiText.Resource(Res.string.evaluation_place_block, listOf(3)), "MYS-116")
			),
			place.placeText
		)
		assertEquals("9:30–10:30", place.timeText)
	}

	@Test
	fun toEvaluationPlaceItem_withoutARoom_leavesTheRoomOut() {
		val place = meeting(day = MONDAY, start = 1, end = 2, classroom = "").toEvaluationPlaceItem()

		assertEquals(UiText.Resource(Res.string.evaluation_place_blocks, listOf(1, 2)), place.placeText)
	}

	@Test
	fun toEvaluationPlaceItem_ofABlockPastTheDay_hasNoHours() {
		// Block 17 starts at 23:30 and ends past midnight: an hour for it would be wrong.
		assertNull(meeting(day = MONDAY, start = 17).toEvaluationPlaceItem().timeText)
		assertNull(meeting(day = MONDAY, start = 15, end = 17).toEvaluationPlaceItem().timeText)
		assertNotNull(meeting(day = MONDAY, start = 15, end = 16).toEvaluationPlaceItem().timeText)
	}

	@Test
	fun inheritedEvaluationPlace_isTheFirstMeetingOfThatWeekday() {
		val meetings = listOf(meeting(MONDAY, 5, 6), meeting(MONDAY, 1, 2))

		val place = inheritedEvaluationPlace(meetings, EvaluationScheduleMode.DATED, MONDAY_DATE)

		assertEquals(meetings[1].toEvaluationPlaceItem(), place)
		assertNull(inheritedEvaluationPlace(meetings, EvaluationScheduleMode.DATED, TUESDAY_DATE))
	}

	@Test
	fun inheritedEvaluationPlace_isNullForAContinuousEvaluationOrAnAttemptWithoutSchedule() {
		val meetings = listOf(meeting(MONDAY, 1, 2))

		assertNull(inheritedEvaluationPlace(meetings, EvaluationScheduleMode.CONTINUOUS, MONDAY_DATE))
		assertNull(inheritedEvaluationPlace(meetings, EvaluationScheduleMode.DATED, null))
		assertNull(inheritedEvaluationPlace(emptyList(), EvaluationScheduleMode.DATED, MONDAY_DATE))
	}

	@Test
	fun hasClassOn_marksOnlyTheDaysTheSubjectMeets_whateverTheWeekday() {
		val meetings = listOf(meeting(SUNDAY, 1), meeting(SATURDAY, 2))

		assertTrue(meetings.hasClassOn(LocalDate(2026, 10, 11)))
		assertTrue(meetings.hasClassOn(LocalDate(2026, 10, 17)))
		assertFalse(meetings.hasClassOn(MONDAY_DATE))
		assertFalse(emptyList<ScheduleMeeting>().hasClassOn(MONDAY_DATE))
	}

	@Test
	fun classPlaceOn_saysInClassOnADayWithClassAndNothingOtherwise() {
		val meetings = listOf(meeting(MONDAY, 1, 2))

		val place = assertNotNull(meetings.classPlaceOn(MONDAY_DATE))

		assertEquals(
			UiText.Resource(Res.string.evaluation_place_in_class, listOf(meetings[0].toEvaluationPlaceItem().placeText)),
			place.placeText
		)
		assertEquals("7:30–9:30", place.timeText)
		assertNull(meetings.classPlaceOn(TUESDAY_DATE))
	}

	@Test
	fun toEvaluationItemList_carriesThePlaceOfADatedEvaluationOnAClassDay() = runTest {
		val attempt = DEFAULT_EVALUATION_SUBJECT.copy(meetings = listOf(meeting(MONDAY, 1, 2)))
		val onClassDay = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = MONDAY_DATE.toEvaluationEpochMillis()
		)
		val offClassDay = onClassDay.copy(id = "evaluation-off", date = TUESDAY_DATE.toEvaluationEpochMillis())
		val continuous = onClassDay.copy(
			id = "evaluation-continuous",
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = null
		)

		val items = listOf(onClassDay, offClassDay, continuous)
			.toEvaluationItemList(mapping = testEvaluationItemMapping(), attempts = listOf(attempt))
			.flatMap { group -> group.items }
			.associateBy { item -> item.evaluationId }

		assertEquals(attempt.meetings[0].toEvaluationPlaceItem(), items.getValue(onClassDay.id).place)
		assertNull(items.getValue(offClassDay.id).place)
		assertNull(items.getValue(continuous.id).place)
	}

	@Test
	fun toEvaluationItemList_ofATermWithoutScheduleCarriesNoPlace() = runTest {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = MONDAY_DATE.toEvaluationEpochMillis()
		)
		val attempt: EditableAttemptDescriptor = DEFAULT_EVALUATION_SUBJECT

		val item = listOf(evaluation)
			.toEvaluationItemList(mapping = testEvaluationItemMapping(), attempts = listOf(attempt))
			.single().items.single()

		assertNull(item.place)
	}
}
