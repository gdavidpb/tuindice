package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryPointsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryPointsRowViewUiTest {
	@Test
	fun when_subjectHasGrades_then_rowShowsTheChipAndItsPoints() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "CI5311", earned = "41"), onClick = {})
		}

		onNodeWithText("CI5311").assertIsDisplayed()
		onNodeWithText("41 de 60 puntos").assertIsDisplayed()
	}

	@Test
	fun when_subjectHasNoGrades_then_rowSaysSinNotasAunAndNotAnEvaluatedFigure() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "EG1114"), onClick = {})
		}

		onNodeWithText("EG1114").assertIsDisplayed()
		onNodeWithText("Sin notas aún").assertIsDisplayed()
		onAllNodesWithContentDescription("evaluados", substring = true, useUnmergedTree = true).assertCountEquals(0)
	}

	@Test
	fun when_subjectHasNoGradesYetHasABar_then_theBarStaysEmpty() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "EG1114"), onClick = {})
		}

		onNodeWithTag(SummaryUiTags.termPointsBar("EG1114"), useUnmergedTree = true).assertIsDisplayed()
		assertEquals(0, partWidth(SummaryEvaluatedBarUiTags.earned("EG1114")))
		assertEquals(0, partWidth(SummaryUiTags.termPointsBarFill("EG1114")))
	}

	@Test
	fun when_subjectHasNoGradesButANextEvaluation_then_theNextLineStays() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(
				item = summaryPointsItem(subjectCode = "EG1114", nextText = "Próx.: Parcial 1 · mar 20"),
				onClick = {}
			)
		}

		onNodeWithText("Próx.: Parcial 1 · mar 20").assertIsDisplayed()
		onAllNodesWithContentDescription("evaluados", substring = true, useUnmergedTree = true).assertCountEquals(0)
	}

	@Test
	fun when_subjectHasNoEvaluations_then_rowSaysOnlySinEvaluaciones() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(
				item = summaryPointsItem(subjectCode = "CI4325", hasEvaluations = false),
				onClick = {}
			)
		}

		onNodeWithText("Sin evaluaciones").assertIsDisplayed()
		onAllNodesWithContentDescription("evaluados", substring = true, useUnmergedTree = true).assertCountEquals(0)
		onNodeWithTag(SummaryUiTags.termPointsBar("CI4325"), useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_subjectHasGrades_then_theReaderHearsWhatIsEvaluatedWithThePointsOfTheRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "CI5311", earned = "41"), onClick = {})
		}

		// The row is one element for the reader: its points and what it has evaluated are in it together.
		onNode(hasText("41 de 60 puntos") and hasContentDescription("60 de 100 evaluados")).assertExists()
	}

	@Test
	fun when_thereIsANextEvaluation_then_rowNamesItAndItsDay_andDoesNotDrawWhatIsEvaluated() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(
				item = summaryPointsItem(
					subjectCode = "CI5311",
					earned = "41",
					nextText = "Próx.: Parcial 2 · lun 26"
				),
				onClick = {}
			)
		}

		onNodeWithText("Próx.: Parcial 2 · lun 26").assertIsDisplayed()
		onNodeWithText("60 de 100 evaluados", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_thereIsNoNextEvaluation_then_rowSaysNothingOfOne() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "CI5311", earned = "41"), onClick = {})
		}

		onAllNodesWithText("Próx.", substring = true).assertCountEquals(0)
		onNodeWithText("60 de 100 evaluados", useUnmergedTree = true).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.termPointsBar("CI5311"), useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theChipIsNotSqueezedAndEachLineIsWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryPointsRowView(
						item = summaryPointsItem(
							subjectCode = "CI5437",
							earned = "18.50",
							evaluated = "125.50",
							nextText = "Próx.: Parcial 2 · lun 26"
						),
						onClick = {}
					)
				}
			}
		}

		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("18.50 de 125.50 puntos")
		assertTextIsWhole("Próx.: Parcial 2 · lun 26")
	}

	@Test
	fun when_theRowIsTapped_then_itAsksForItsSubjectOnRecord() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummaryPointsRowView(
				item = summaryPointsItem(subjectCode = "CI5311", earned = "41"),
				onClick = { taps++ }
			)
		}

		onNodeWithText("41 de 60 puntos").performClick()

		assertEquals(1, taps)
	}

	// The width of a part of the bar, in pixels: a part with no share of the track has none.
	private fun ComposeUiTest.partWidth(tag: String): Int {
		return onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().size.width
	}
}
