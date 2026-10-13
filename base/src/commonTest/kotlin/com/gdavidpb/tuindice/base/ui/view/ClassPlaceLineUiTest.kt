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
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ClassPlaceLineUiTest {
	@Test
	fun when_thePlaceHasHours_then_theHoursFollowTheBlocks() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClassPlaceLine(place = classPlaceItem(1, 2, "MYS-116", clockRange = 450..570))
		}

		awaitText("Bloques 1-2 · MYS-116")
		onNodeWithText("Bloques 1-2 · MYS-116").assertIsDisplayed()
		onNodeWithText("7:30–9:30 a. m.").assertIsDisplayed()

		val blocks = onNodeWithText("Bloques 1-2 · MYS-116").getUnclippedBoundsInRoot()
		val hours = onNodeWithText("7:30–9:30 a. m.").getUnclippedBoundsInRoot()

		// The hours come after the blocks, never before them.
		assertTrue(hours.left >= blocks.right || hours.top >= blocks.bottom, "the hours must follow the blocks")
	}

	@Test
	fun when_thePlaceHasNoHours_then_onlyTheBlocksAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClassPlaceLine(place = classPlaceItem(17, 17, "", clockRange = null))
		}

		awaitText("Bloque 17")
		onNodeWithText("Bloque 17").assertIsDisplayed()
		onAllNodesWithText("a. m.", substring = true).assertCountEquals(0)
		onAllNodesWithText("p. m.", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_theHoursAreInTheMorning_then_theSuffixIsSaidOnceAtTheEnd() = runTuIndiceUiTest {
		assertHours("7:30–9:30 a. m.", clockRange = 450..570)
	}

	@Test
	fun when_theHoursAreInTheAfternoon_then_theyAreOnATwelveHourClockWithNoZeroBeforeTheHour() = runTuIndiceUiTest {
		assertHours("1:30–3:30 p. m.", clockRange = 810..930)
	}

	@Test
	fun when_theHoursCrossNoon_then_eachEndSaysItsSuffix() = runTuIndiceUiTest {
		assertHours("11:30 a. m.–1:30 p. m.", clockRange = 690..810)
	}

	@Test
	fun when_theHoursReachTwelveThirty_then_itIsTwelveThirtyPm() = runTuIndiceUiTest {
		assertHours("12:30–1:30 p. m.", clockRange = 750..810)
	}

	@Test
	fun when_theHoursEndAtTwelveThirtyFromTheMorning_then_eachEndSaysItsSuffix() = runTuIndiceUiTest {
		assertHours("10:30 a. m.–12:30 p. m.", clockRange = 630..750)
	}

	@Test
	fun when_thePlaceDoesNotFit_then_itWrapsInsteadOfBeingCut() = runTuIndiceUiTest {
		val place = "Bloques 10-11 · LAB-FIS-201 · Edificio de Laboratorios"

		setTuIndiceTestContent {
			ClassPlaceLine(
				modifier = Modifier.width(120.dp),
				place = ClassPlaceItem(placeText = UiText.Raw(place), timeText = UiText.Raw("4:30–6:30 p. m."))
			)
		}

		val layout = mutableListOf<TextLayoutResult>()

		onNodeWithText(place).assertIsDisplayed()
		onNodeWithText(place).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layout)

		// It takes more than one line and none of it is replaced by an ellipsis.
		assertTrue(layout.single().lineCount > 1, "the place must wrap")
		assertFalse(layout.single().hasVisualOverflow, "the place must not be cut")
		onNodeWithText("4:30–6:30 p. m.").assertIsDisplayed()
	}

	private fun ComposeUiTest.assertHours(expected: String, clockRange: IntRange) {
		setTuIndiceTestContent {
			ClassPlaceLine(place = classPlaceItem(1, 2, "MYS-116", clockRange = clockRange))
		}

		awaitText(expected)
		onNodeWithText(expected).assertIsDisplayed()
	}

	// Resources resolve asynchronously off Android: wait for the text before reading its node.
	private fun ComposeUiTest.awaitText(text: String) {
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
	}
}
