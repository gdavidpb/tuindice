package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsFigureViewUiTest {
	@Test
	fun when_theFigureHasAShare_then_itsValueLabelAndShareAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsFigureView(figure = figureOf(value = "10", label = "de 400 puntos", trailing = "3 %"))
		}

		onNodeWithText("10").assertIsDisplayed()
		onNodeWithText("de 400 puntos").assertIsDisplayed()
		onNodeWithText("3 %").assertIsDisplayed()
	}

	@Test
	fun when_theFigureHasNoShare_then_onlyItsValueAndLabelAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsFigureView(figure = figureOf(value = "47", label = "materias cursadas"))
		}

		onNodeWithText("47").assertIsDisplayed()
		onNodeWithText("materias cursadas").assertIsDisplayed()
		onAllNodesWithText("3 %").assertCountEquals(0)
	}
}
