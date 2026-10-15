package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.firstBaselineInRoot
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryEntryViewUiTest {
	private val place = ClassPlaceItem(
		room = "MYS-210",
		scheduleText = UiText.Raw("3-4 · 9:30–11:30 a. m.")
	)

	@Test
	fun when_entryHasTitleAndPlace_then_theSubjectIsBesideTheTitle_andThePlaceIsUnderIt() = runTuIndiceUiTest {
		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Expanded) {
			Box(modifier = Modifier.width(600.dp)) {
				Entry(subjectCode = "CI5437", place = place)
			}
		}

		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val subject = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val room = onNodeWithText("MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val schedule = onNodeWithText("3-4 · 9:30–11:30 a. m.", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(subject.left >= title.right, "the subject is written after the title")
		assertTrue(abs(subject.center.y - title.center.y) <= 1f, "the subject shares the line of the title")
		assertTrue(room.top >= title.bottom, "the room is on the line under the title")
		assertTrue(schedule.top >= room.bottom, "the schedule is under the room")
	}

	@Test
	fun when_entryHasNoPlace_then_itShowsOnlyTheTitleAndItsSubject() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Entry(subjectCode = "EG1114", place = null)
		}

		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val subject = onNodeWithText("EG1114", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(subject.left >= title.right, "the subject is written after the title")
		onNodeWithText("MYS-210", useUnmergedTree = true).assertDoesNotExist()
		onNodeWithText("3-4 · 9:30–11:30 a. m.", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_leadIsDrawn_then_itsBaselineIsTheBaselineOfTheFirstLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Entry(subjectCode = "CI5437", place = place)
		}

		val lead = onNodeWithTag("lead", useUnmergedTree = true).fetchSemanticsNode()
		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode()
		val room = onNodeWithText("MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		val leadBaseline = lead.firstBaselineInRoot()
		val titleBaseline = title.firstBaselineInRoot()

		assertTrue(
			abs(leadBaseline - titleBaseline) <= 1f,
			"the lead's baseline is $leadBaseline and the title's is $titleBaseline"
		)
		assertTrue(leadBaseline < room.top, "the lead's baseline is on the first line, above the place")
	}

	@Test
	fun when_theEntryIsTallerThanItsContent_then_itsBlockIsCenteredInIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Entry(subjectCode = "CI5437", place = place, modifier = Modifier.testTag("entry").height(160.dp))
		}

		val entry = onNodeWithTag("entry", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val schedule = onNodeWithText("3-4 · 9:30–11:30 a. m.", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		val above = title.top - entry.top
		val below = entry.bottom - schedule.bottom

		assertTrue(above > 20f, "the block is not at the top of the taller row: $above px above it")
		assertTrue(abs(above - below) <= 8f, "the block is centered: $above px above it and $below px below it")
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

		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val room = onNodeWithText("MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTextIsWhole("PARCIAL 1")
		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("MYS-210", singleLine = true)
		assertTextIsWhole("3-4 · 9:30–11:30 a. m.")
		assertTrue(room.top >= title.bottom, "the place does not fit beside the title, so it goes down to the next line")
	}

	// The shape with fixed parts: a day as the lead and a type icon with a name and its subject as the title line.
	@Composable
	private fun Entry(subjectCode: String, place: ClassPlaceItem?, modifier: Modifier = Modifier) {
		SummaryEntryView(
			modifier = modifier,
			lead = {
				SummaryLeadTextView(
					modifier = Modifier.alignBy(FirstBaseline).testTag("lead"),
					text = "Hoy"
				)
			},
			titleLine = {
				SummaryTitleLine(
					icon = Icons.Outlined.FileCopy,
					iconTag = "title_icon",
					titleText = "PARCIAL 1",
					subjectCode = subjectCode
				)
			},
			place = place
		)
	}
}
