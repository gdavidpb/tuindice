package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryEvaluationRowItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryEvaluationRowViewUiTest {
	@Test
	fun when_evaluationHasAPlace_then_rowShowsDayTitleChipAndBlocksButNoHour() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(item = summaryEvaluationRowItem(), onClick = {})
		}

		onNodeWithText("Hoy", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("Parcial 1", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("CI5437", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("9:30–11:30 a. m.", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_subjectHasNoClassThatDay_then_rowHasNoPlaceLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(
				item = summaryEvaluationRowItem(
					leadText = "Vie 16",
					titleText = "Ensayo 1",
					subjectCode = "EG1114",
					hasPlace = false
				),
				onClick = {}
			)
		}

		onNodeWithText("Vie 16", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("Ensayo 1", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_rowIsTapped_then_itAsksForItsWeek_andSaysSoToAScreenReader() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummaryEvaluationRowView(
				modifier = Modifier.testTag("row"),
				item = summaryEvaluationRowItem(),
				onClick = { taps++ }
			)
		}

		onNodeWithTag("row").assertHasClickAction()
		assertEquals(
			"Ver las evaluaciones de esa semana",
			onNodeWithTag("row").fetchSemanticsNode().config[SemanticsActions.OnClick].label
		)

		onNodeWithTag("row").performClick()

		assertEquals(1, taps)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_noTextIsCutAndTheChipIsWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryEvaluationRowView(
						item = summaryEvaluationRowItem(titleText = "Trabajo escrito 12", leadText = "Mié 28"),
						onClick = {}
					)
				}
			}
		}

		assertTextIsWhole("Mié 28", singleLine = true)
		assertTextIsWhole("Trabajo escrito 12")
		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("3-4 · MYS-210")
	}
}
