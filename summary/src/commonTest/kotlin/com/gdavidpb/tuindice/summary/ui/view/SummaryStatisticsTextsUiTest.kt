package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsTextsUiTest {
	@Test
	fun when_aTooltipIsGiven_then_itsTitleAndLinesAreTheWordsItShows() = runTuIndiceUiTest {
		var tooltip: RichTooltipText? = null

		setTuIndiceTestContent {
			tooltip = tooltipOf("Quices: 8 de 10 (80 %)", "1 de 1 calificada").toRichText()
		}
		waitForIdle()

		assertEquals(
			RichTooltipText(title = "Quices: 8 de 10 (80 %)", lines = listOf("1 de 1 calificada")),
			tooltip
		)
	}

	@Test
	fun when_aFigureIsGiven_then_itsValueAndWhatItCountsAreOnePhrase() = runTuIndiceUiTest {
		var phrase = ""

		setTuIndiceTestContent {
			phrase = figureOf(value = "4.1250", label = "acumulado").summaryText()
		}
		waitForIdle()

		assertEquals("4.1250 acumulado", phrase)
	}
}
