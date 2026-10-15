package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsTypeRowContentUiTest {
	@Test
	fun when_aTypeIsGiven_then_itsNameItsCountAndItsScoreAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ContentHost(scoreText = "8 de 10 · 80 %")
		}
		waitForIdle()

		onNodeWithText("Quices").assertIsDisplayed()
		onNodeWithText("1 de 1 calificada").assertIsDisplayed()
		onNodeWithText("8 de 10 · 80 %").assertIsDisplayed()
	}

	@Test
	fun when_aTypeHasNoScoreYet_then_ItSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ContentHost(scoreText = "Sin notas aún")
		}
		waitForIdle()

		onNodeWithText("Sin notas aún").assertIsDisplayed()
	}

	@Composable
	private fun ContentHost(scoreText: String) {
		Column {
			SummaryStatisticsTypeRowContent(
				row = SummaryStatisticsEvaluationTypeItem(
					name = words("Quices"),
					gradedText = words("1 de 1 calificada"),
					scoreText = words(scoreText),
					fraction = 0.8f,
					tooltip = null
				)
			)
		}
	}
}
