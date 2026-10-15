package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.class_place_with_time
import tuindice.base.generated.resources.class_time_range
import tuindice.base.generated.resources.class_time_range_same_period
import tuindice.base.generated.resources.date_time_am
import tuindice.base.generated.resources.date_time_pm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// What classPlaceItem and toClockRangeText describe: the template, its numbers and the half of the
// day by their place in the resources. The Spanish they read as is asserted in ClassPlaceLinesUiTest.
class ClassPlaceTest {
	private val am = UiText.Resource(Res.string.date_time_am)
	private val pm = UiText.Resource(Res.string.date_time_pm)

	@Test
	fun classPlaceItem_withARoom_carriesTheRoomAlone() {
		val place = classPlaceItem(startBlock = 1, endBlock = 2, classroom = "MYS-116", clockRange = null)

		assertEquals("MYS-116", place.room)
	}

	@Test
	fun classPlaceItem_withoutARoom_hasNoRoom_whetherTheRoomIsEmptyOrBlank() {
		assertNull(classPlaceItem(startBlock = 1, endBlock = 2, classroom = "", clockRange = null).room)
		assertNull(classPlaceItem(startBlock = 5, endBlock = 5, classroom = "  ", clockRange = null).room)
	}

	@Test
	fun classPlaceItem_ofSeveralBlocks_withoutHours_isTheRangeAlone() {
		assertEquals(
			UiText.Raw("1-2"),
			classPlaceItem(startBlock = 1, endBlock = 2, classroom = "MYS-116", clockRange = null).scheduleText
		)
	}

	@Test
	fun classPlaceItem_ofASingleBlock_withoutHours_isItsNumberAlone() {
		assertEquals(
			UiText.Raw("3"),
			classPlaceItem(startBlock = 3, endBlock = 3, classroom = "MYS-116", clockRange = null).scheduleText
		)
	}

	@Test
	fun classPlaceItem_withAClockRange_saysTheBlocksAndTheirHours() {
		val place = classPlaceItem(startBlock = 1, endBlock = 2, classroom = "MYS-116", clockRange = 450..570)

		assertEquals(
			UiText.Resource(
				Res.string.class_place_with_time,
				listOf("1-2", (450..570).toClockRangeText())
			),
			place.scheduleText
		)
	}

	@Test
	fun classPlaceItem_outsideTheDay_hasTheBlocksAloneAndNoHours() {
		// A block that falls outside the day has no range: an hour for it would be wrong, not late.
		assertEquals(
			UiText.Raw("17"),
			classPlaceItem(startBlock = 17, endBlock = 17, classroom = "MYS-116", clockRange = null).scheduleText
		)
	}

	@Test
	fun toClockRangeText_inTheMorning_saysTheSuffixOnceAtTheEnd() {
		assertEquals(
			UiText.Resource(Res.string.class_time_range_same_period, listOf("7:30", "9:30", am)),
			(7 * 60 + 30..9 * 60 + 30).toClockRangeText()
		)
	}

	@Test
	fun toClockRangeText_inTheAfternoon_usesTheTwelveHourClockWithoutAZeroBeforeTheHour() {
		assertEquals(
			UiText.Resource(Res.string.class_time_range_same_period, listOf("1:30", "3:30", pm)),
			(13 * 60 + 30..15 * 60 + 30).toClockRangeText()
		)
	}

	@Test
	fun toClockRangeText_acrossNoon_saysTheSuffixOnEachEnd() {
		assertEquals(
			UiText.Resource(Res.string.class_time_range, listOf("11:30", am, "1:30", pm)),
			(11 * 60 + 30..13 * 60 + 30).toClockRangeText()
		)
	}

	@Test
	fun toClockRangeText_atTwelveThirty_isTwelveThirtyPm() {
		assertEquals(
			UiText.Resource(Res.string.class_time_range_same_period, listOf("12:30", "1:30", pm)),
			(12 * 60 + 30..13 * 60 + 30).toClockRangeText()
		)
		// A class from the morning that ends at 12:30 crosses noon.
		assertEquals(
			UiText.Resource(Res.string.class_time_range, listOf("10:30", am, "12:30", pm)),
			(10 * 60 + 30..12 * 60 + 30).toClockRangeText()
		)
	}

	@Test
	fun toClockRangeText_pastTheLastBlock_staysInTheEvening() {
		assertEquals(
			UiText.Resource(Res.string.class_time_range_same_period, listOf("10:30", "11:30", pm)),
			(22 * 60 + 30..23 * 60 + 30).toClockRangeText()
		)
	}
}
