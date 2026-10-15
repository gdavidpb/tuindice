package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryDetailsLineUiTest {
	private val place = ClassPlaceItem(
		placeText = UiText.Raw("3-4 · MYS-210"),
		timeText = UiText.Raw("9:30–11:30 a. m.")
	)

	@Test
	fun when_theLineIsWide_then_thePlaceIsBesideTheChip_onItsLine() = runTuIndiceUiTest {
		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Expanded) {
			Box(modifier = Modifier.width(600.dp)) {
				SummaryDetailsLine(subjectCode = "CI5437", place = place)
			}
		}

		val chip = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val blocks = onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(blocks.left >= chip.right, "the place is written after the chip")
		assertTrue(abs(blocks.center.y - chip.center.y) <= 1f, "the place shares the line of the chip")
	}

	@Test
	fun when_theLineIsNarrow_then_thePlaceGoesDownWhole_andNothingIsCut() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(160.dp)) {
				SummaryDetailsLine(subjectCode = "CI5437", place = place)
			}
		}

		val chip = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val blocks = onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(blocks.top >= chip.bottom, "the place does not fit beside the chip, so it goes down under it")
		assertTextIsWhole("3-4 · MYS-210")
		assertTextIsWhole("9:30–11:30 a. m.", singleLine = true)
	}

	@Test
	fun when_theSubjectHasNoClassThatDay_then_onlyTheChipIsDrawn_andItStaysWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(200.dp)) {
					SummaryDetailsLine(subjectCode = "CI5437", place = null)
				}
			}
		}

		assertTextIsWhole("CI5437", singleLine = true)
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertDoesNotExist()
	}
}
