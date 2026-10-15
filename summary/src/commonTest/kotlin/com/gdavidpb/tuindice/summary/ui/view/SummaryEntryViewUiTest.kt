package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
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
class SummaryEntryViewUiTest {
	private val place = ClassPlaceItem(
		placeText = UiText.Raw("3-4 · MYS-210"),
		timeText = UiText.Raw("9:30–11:30 a. m.")
	)

	@Test
	fun when_entryHasTitleAndPlace_then_chipAndPlaceShareALine_andTheHourFollowsTheBlocks() = runTuIndiceUiTest {
		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Expanded) {
			Box(modifier = Modifier.width(600.dp)) {
				Entry(subjectCode = "CI5437", place = place)
			}
		}

		val subject = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val blocks = onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val hour = onNodeWithText("9:30–11:30 a. m.", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(blocks.left >= subject.right, "the place is written after the chip")
		assertTrue(abs(blocks.center.y - subject.center.y) <= 1f, "the place shares the line of the chip")
		assertTrue(hour.left >= blocks.right, "the hour comes after the blocks")
	}

	@Test
	fun when_entryHasNoPlace_then_itShowsOnlyTheChip_underTheTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Entry(subjectCode = "EG1114", place = null)
		}

		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val chip = onNodeWithText("EG1114", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(chip.top >= title.bottom, "the chip is on the line under the title")
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_leadIsDrawn_then_itIsCenteredWithTheFirstLine_notWithTheWholeEntry() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Entry(subjectCode = "CI5437", place = place)
		}

		val lead = onNodeWithTag("lead", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val subject = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(abs(lead.center.y - title.center.y) <= 1f, "the lead is centered with the title")
		assertTrue(lead.center.y < subject.top, "the lead is not centered with the whole entry")
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_nothingIsCut_andThePlaceGoesDownWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					Entry(subjectCode = "CI5437", place = place)
				}
			}
		}

		val subject = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val blocks = onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTextIsWhole("PARCIAL 1")
		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("3-4 · MYS-210")
		assertTextIsWhole("9:30–11:30 a. m.", singleLine = true)
		assertTrue(blocks.top >= subject.bottom, "the place does not fit beside the chip, so it goes down to the next line")
	}

	// The shape with fixed parts: a day as the lead and a type icon with a name as the title line.
	@Composable
	private fun Entry(subjectCode: String, place: ClassPlaceItem?) {
		SummaryEntryView(
			lead = {
				SummaryLeadTextView(modifier = Modifier.testTag("lead"), text = "Hoy")
			},
			titleLine = {
				SummaryTitleLine(
					icon = Icons.Outlined.FileCopy,
					iconTag = "title_icon",
					titleText = "PARCIAL 1"
				)
			},
			subjectCode = subjectCode,
			place = place
		)
	}
}
