package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsCreditsDataUiTest {
	@Test
	fun when_creditsAreGiven_then_theFigureAndTheBarAreDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost()
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsFigure)
		assertNodeVisible(SummaryUiTags.StatisticsPensumTrack, useUnmergedTree = true)
	}

	@Test
	fun when_theBarIsDrawn_then_aScreenReaderHearsTheFigureAndItsShareAsOneSentence() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost()
		}
		waitForIdle()

		val bar = onNodeWithTag(SummaryUiTags.StatisticsPensumTrack, useUnmergedTree = true)
		bar.assertContentDescriptionEquals("Avance del pensum: 152 de 205 créditos, 74 %")
		assertEquals(0.74f, bar.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current)
	}

	@Composable
	private fun DataHost() {
		Column {
			SummaryStatisticsCreditsData(
				data = SummaryStatisticsCreditsItem.Content.Data(
					figure = figureOf(value = "152", label = "de 205 créditos", trailing = "74 %"),
					fraction = 0.74f,
					description = words("Avance del pensum: 152 de 205 créditos, 74 %")
				)
			)
		}
	}
}
