package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryPointsItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryPointsRowViewUiTest {
	@Test
	fun when_subjectHasGrades_then_rowShowsTheChipAndItsPoints() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "CI5311", earned = "41"))
		}

		onNodeWithText("CI5311").assertIsDisplayed()
		onNodeWithText("41 de 60 puntos").assertIsDisplayed()
	}

	@Test
	fun when_subjectHasNoGrades_then_rowSaysSinNotasAun() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPointsRowView(item = summaryPointsItem(subjectCode = "EG1114"))
		}

		onNodeWithText("EG1114").assertIsDisplayed()
		onNodeWithText("Sin notas aún").assertIsDisplayed()
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theChipIsNotSqueezedAndThePointsAreWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryPointsRowView(
						item = summaryPointsItem(subjectCode = "CI5437", earned = "18.50", evaluated = "125.50")
					)
				}
			}
		}

		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("18.50 de 125.50 puntos")
	}
}
