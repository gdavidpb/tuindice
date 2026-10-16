package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_place_in_class
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
	fun toClassPlaceItem_ofSeveralBlocks_saysTheRoomAndTheRangeWithItsHours() {
		val place = meeting(day = MONDAY, start = 1, end = 2).toClassPlaceItem()

		// Block 1 starts at 7:30 and every block lasts an hour: the last block ends at 9:30. How the
		// room and the schedule are said is the base's (ClassPlaceTest); this is that the meeting's
		// own numbers reach it.
		assertEquals(
			classPlaceItem(startBlock = 1, endBlock = 2, classroom = "MYS-116", clockRange = 450..570),
			place
		)
	}

	@Test
	fun toClassPlaceItem_ofASingleBlock_hasItsOwnHour() {
		val place = meeting(day = MONDAY, start = 3).toClassPlaceItem()

		assertEquals(
			classPlaceItem(startBlock = 3, endBlock = 3, classroom = "MYS-116", clockRange = 570..630),
			place
		)
	}

	@Test
	fun toClassPlaceItem_withoutARoom_leavesTheRoomOut() {
		val place = meeting(day = MONDAY, start = 1, end = 2, classroom = "").toClassPlaceItem()

		assertNull(place.room)
		assertEquals(
			classPlaceItem(startBlock = 1, endBlock = 2, classroom = "", clockRange = 450..570).scheduleText,
			place.scheduleText
		)
	}

	@Test
	fun toClassPlaceItem_ofABlockPastTheDay_hasNoHours() {
		// Block 17 starts at 23:30 and ends past midnight: an hour for it would be wrong.
		assertEquals(UiText.Raw("17"), meeting(day = MONDAY, start = 17).toClassPlaceItem().scheduleText)
		assertEquals(UiText.Raw("15-17"), meeting(day = MONDAY, start = 15, end = 17).toClassPlaceItem().scheduleText)
		assertTrue(meeting(day = MONDAY, start = 15, end = 16).toClassPlaceItem().scheduleText is UiText.Resource)
	}

	@Test
	fun inheritedEvaluationPlace_isTheFirstMeetingOfThatWeekday() {
		val meetings = listOf(meeting(MONDAY, 5, 6), meeting(MONDAY, 1, 2))

		val place = inheritedEvaluationPlace(meetings, EvaluationScheduleMode.DATED, MONDAY_DATE)

		assertEquals(meetings[1].toClassPlaceItem(), place)
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
			UiText.Resource(Res.string.evaluation_place_in_class, listOf(meetings[0].toClassPlaceItem().scheduleText)),
			place.scheduleText
		)
		assertEquals(meetings[0].toClassPlaceItem().room, place.room)
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

		assertEquals(attempt.meetings[0].toClassPlaceItem(), items.getValue(onClassDay.id).place)
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
