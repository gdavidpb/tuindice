package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryPointsItem
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
	fun when_subjectHasNoGrades_then_rowSaysSinNotasAun() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "EG1114"), onClick = {})
		}

		onNodeWithText("EG1114").assertIsDisplayed()
		onNodeWithText("Sin notas aún").assertIsDisplayed()
	}

	@Test
	fun when_subjectHasNoEvaluations_then_rowSaysSinEvaluacionesAndNothingIsEvaluated() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(
				item = summaryPointsItem(subjectCode = "CI4325", hasEvaluations = false),
				onClick = {}
			)
		}

		onNodeWithText("Sin evaluaciones").assertIsDisplayed()
		onNodeWithText("0 de 100 evaluados").assertIsDisplayed()
	}

	@Test
	fun when_thereIsANextEvaluation_then_rowNamesItAndItsDay_besideWhatIsEvaluated() = runTuIndiceUiTest {
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
		onNodeWithText("60 de 100 evaluados").assertIsDisplayed()
	}

	@Test
	fun when_thereIsNoNextEvaluation_then_rowSaysNothingOfOne() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "CI5311", earned = "41"), onClick = {})
		}

		onAllNodesWithText("Próx.", substring = true).assertCountEquals(0)
		onNodeWithText("60 de 100 evaluados").assertIsDisplayed()
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
		assertTextIsWhole("100 de 100 evaluados")
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
}
