package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationPlaceItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationPlaceLineUiTest {
	@Test
	fun when_thePlaceHasHours_then_theHoursFollowTheBlocks() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationPlaceLine(
				place = EvaluationPlaceItem(
					placeText = UiText.Raw("Bloques 1-2 · MYS-116"),
					timeText = "7:30–9:30"
				)
			)
		}

		onNodeWithText("Bloques 1-2 · MYS-116").assertIsDisplayed()
		onNodeWithText("7:30–9:30").assertIsDisplayed()

		val blocks = onNodeWithText("Bloques 1-2 · MYS-116").getUnclippedBoundsInRoot()
		val hours = onNodeWithText("7:30–9:30").getUnclippedBoundsInRoot()

		// The hours come after the blocks, never before them.
		assertTrue(
			hours.left >= blocks.right || hours.top >= blocks.bottom,
			"the hours must follow the blocks"
		)
	}

	@Test
	fun when_thePlaceHasNoHours_then_onlyTheBlocksAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationPlaceLine(
				place = EvaluationPlaceItem(placeText = UiText.Raw("Bloque 17"), timeText = null)
			)
		}

		onNodeWithText("Bloque 17").assertIsDisplayed()
		onNodeWithText("7:30–9:30").assertDoesNotExist()
	}
}
