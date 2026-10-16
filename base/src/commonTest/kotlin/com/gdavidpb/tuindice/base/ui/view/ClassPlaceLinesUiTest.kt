package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ClassPlaceLinesUiTest {
	@Test
	fun when_theClassHasARoomAndHours_then_theRoomIsOnItsOwnLine_andTheScheduleUnderIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClassPlaceLines(place = classPlaceItem(1, 2, "MYS-116", clockRange = 450..570))
		}

		awaitText("MYS-116")
		onNodeWithText("MYS-116").assertIsDisplayed()
		onNodeWithText(MORNING_SCHEDULE).assertIsDisplayed()

		val room = onNodeWithText("MYS-116").getUnclippedBoundsInRoot()
		val schedule = onNodeWithText(MORNING_SCHEDULE).getUnclippedBoundsInRoot()

		// The schedule goes down under the room, and both lines start at the same edge.
		assertTrue(schedule.top >= room.bottom, "the schedule must be under the room")
		assertTrue(abs(schedule.left.value - room.left.value) <= 0.5f, "the schedule must start where the room starts")
	}

	@Test
	fun when_theClassNamesNoRoom_then_theScheduleIsTheOnlyLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClassPlaceLines(place = classPlaceItem(1, 2, "", clockRange = 450..570))
		}

		awaitText(MORNING_SCHEDULE)
		onNodeWithText(MORNING_SCHEDULE).assertIsDisplayed()
		onAllNodesWithText("MYS", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_theBlocksFallOutsideTheDay_then_theBlockAloneIsShown_andNoWordForABlockIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClassPlaceLines(place = classPlaceItem(17, 17, "MYS-116", clockRange = null))
		}

		awaitText("17")
		onNodeWithText("17").assertIsDisplayed()
		onAllNodesWithText("a. m.", substring = true).assertCountEquals(0)
		onAllNodesWithText("p. m.", substring = true).assertCountEquals(0)
		onAllNodesWithText("loque", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_theHoursAreInTheMorning_then_theSuffixIsSaidOnceAtTheEnd() = runTuIndiceUiTest {
		assertSchedule("1-2 · 7:30–9:30 a. m.", clockRange = 450..570)
	}

	@Test
	fun when_theHoursAreInTheAfternoon_then_theyAreOnATwelveHourClockWithNoZeroBeforeTheHour() = runTuIndiceUiTest {
		assertSchedule("1-2 · 1:30–3:30 p. m.", clockRange = 810..930)
	}

	@Test
	fun when_theHoursCrossNoon_then_eachEndSaysItsSuffix() = runTuIndiceUiTest {
		assertSchedule("1-2 · 11:30 a. m.–1:30 p. m.", clockRange = 690..810)
	}

	@Test
	fun when_theHoursReachTwelveThirty_then_itIsTwelveThirtyPm() = runTuIndiceUiTest {
		assertSchedule("1-2 · 12:30–1:30 p. m.", clockRange = 750..810)
	}

	@Test
	fun when_theHoursEndAtTwelveThirtyFromTheMorning_then_eachEndSaysItsSuffix() = runTuIndiceUiTest {
		assertSchedule("1-2 · 10:30 a. m.–12:30 p. m.", clockRange = 630..750)
	}

	@Test
	fun when_theRoomDoesNotFit_then_itWrapsInsteadOfBeingCut() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClassPlaceLines(
				modifier = Modifier.width(120.dp),
				place = classPlaceItem(10, 11, LONG_ROOM, clockRange = 990..1110)
			)
		}

		onNodeWithText(LONG_ROOM).assertIsDisplayed()
		assertWhole(LONG_ROOM)
		onNodeWithText("10-11 · 4:30–6:30 p. m.").assertIsDisplayed()
	}

	private fun ComposeUiTest.assertSchedule(expected: String, clockRange: IntRange) {
		setTuIndiceTestContent {
			ClassPlaceLines(place = classPlaceItem(1, 2, "MYS-116", clockRange = clockRange))
		}

		awaitText(expected)
		onNodeWithText(expected).assertIsDisplayed()
	}

	// A text laid out in more than one line, the whole of it: no line ends in an ellipsis, and the lines
	// hold every character.
	private fun ComposeUiTest.assertWhole(text: String) {
		val layout = mutableListOf<TextLayoutResult>()

		onNodeWithText(text, useUnmergedTree = true)
			.fetchSemanticsNode()
			.config[SemanticsActions.GetTextLayoutResult]
			.action?.invoke(layout)

		val result = layout.single()

		assertTrue(result.lineCount > 1, "'$text' must take more than one line")
		assertEquals(text.length, result.getLineEnd(result.lineCount - 1), "the lines must hold the whole '$text'")
		assertFalse((0 until result.lineCount).any { line -> result.isLineEllipsized(line) }, "'$text' must not be cut")
	}

	// Resources resolve asynchronously off Android: wait for the text before reading its node.
	private fun ComposeUiTest.awaitText(text: String) {
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
		const val MORNING_SCHEDULE = "1-2 · 7:30–9:30 a. m."
		const val LONG_ROOM = "LAB-FIS-201 Edificio de Laboratorios"
	}
}
