package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryTitleLineUiTest {
	@Test
	fun when_theTitleIsLongOnANarrowLine_then_itWrapsWhole_andTheSubjectStaysBesideIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(240.dp)) {
					SummaryTitleLine(
						icon = Icons.Outlined.FileCopy,
						iconTag = "icon",
						titleText = "TRABAJO ESCRITO 12",
						subjectCode = "CI5437",
						modifier = Modifier.testTag("line")
					)
				}
			}
		}

		val title = onNodeWithText("TRABAJO ESCRITO 12", useUnmergedTree = true).fetchSemanticsNode()
		val line = onNodeWithTag("line", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val chip = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTextIsWhole("TRABAJO ESCRITO 12")
		assertTextIsWhole("CI5437", singleLine = true)
		assertTrue(linesOf(title) >= 2, "the title takes two lines on a narrow line, it has ${linesOf(title)}")
		assertTrue(chip.right <= line.right + 1f, "the chip stays inside the line: chip ${chip.right}, line ${line.right}")
	}

	@Test
	fun when_theSubjectIsGiven_then_itsChipIsBesideTheTitle_onItsLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(360.dp)) {
				SummaryTitleLine(
					icon = Icons.Outlined.FileCopy,
					iconTag = "icon",
					titleText = "PARCIAL 1",
					subjectCode = "CI5437"
				)
			}
		}

		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val chip = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		onNodeWithTag("icon", useUnmergedTree = true).assertIsDisplayed()
		assertTrue(chip.left >= title.right, "the chip is written after the title")
		assertTrue(abs(chip.center.y - title.center.y) <= 1f, "the chip shares the line of the title")
	}

	@Test
	fun when_theTitleIsShort_then_itKeepsOneLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(360.dp)) {
				SummaryTitleLine(
					icon = Icons.Outlined.FileCopy,
					iconTag = "icon",
					titleText = "PARCIAL 1",
					subjectCode = "CI5437"
				)
			}
		}

		val title = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode()

		assertEquals(1, linesOf(title), "a short title keeps one line")
	}

	// How many lines the text of the node lays out to, as the layout of the text reports them.
	private fun linesOf(node: SemanticsNode): Int {
		val layouts = mutableListOf<TextLayoutResult>()

		node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)

		return layouts.single().lineCount
	}
}
