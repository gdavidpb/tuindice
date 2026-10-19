package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.toNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// A schedule numbers its days from 1, the Monday, to 7, the Sunday.
class ChatScheduleDayTextTest {
	@Test
	fun when_theDayIsMonday_then_itIsTheFirstNameCapitalized() {
		assertEquals(UiText.Capitalized(DayOfWeek.MONDAY.toNameText()), scheduleDayText(1))
	}

	@Test
	fun when_theDayIsSunday_then_itIsTheLastNameCapitalized() {
		assertEquals(UiText.Capitalized(DayOfWeek.SUNDAY.toNameText()), scheduleDayText(7))
	}

	@Test
	fun when_theDayIsOutsideTheWeek_then_itHasNoName() {
		assertNull(scheduleDayText(0))
		assertNull(scheduleDayText(8))
	}
}
