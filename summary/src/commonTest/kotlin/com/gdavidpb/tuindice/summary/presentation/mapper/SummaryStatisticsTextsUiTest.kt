package com.gdavidpb.tuindice.summary.presentation.mapper

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsTextsUiTest {
	@Test
	fun when_aTooltipIsGiven_then_itsTitleAndLinesAreTheWordsItShows() = runTuIndiceUiTest {
		var tooltip: RichTooltipText? = null

		setTuIndiceTestContent {
			tooltip = SummaryStatisticsTooltipItem(
				title = UiText.Raw("Quices: 8 de 10 (80 %)"),
				lines = listOf(UiText.Raw("1 de 1 calificada"))
			).toRichText()
		}
		waitForIdle()

		assertEquals(
			RichTooltipText(title = "Quices: 8 de 10 (80 %)", lines = listOf("1 de 1 calificada")),
			tooltip
		)
	}

	@Test
	fun when_aFigureLeadsAChart_then_itsValueAndWhatItCountsAreOnePhrase() = runTuIndiceUiTest {
		var phrase = ""

		setTuIndiceTestContent {
			phrase = chartSummaryOf(
				SummaryStatisticsFigureItem(value = "4.1250", label = UiText.Raw("acumulado"))
			).asString()
		}
		waitForIdle()

		assertEquals("4.1250 acumulado", phrase)
	}

	@Test
	fun when_aBlockHasNoExplanation_then_itsInfoIsAbsent() = runTuIndiceUiTest {
		var info: RichTooltipText? = RichTooltipText(lines = listOf("unset"))

		setTuIndiceTestContent {
			info = (null as UiText?).toBlockInfo()
		}
		waitForIdle()

		assertNull(info)
	}

	@Test
	fun when_aBlockHasAnExplanation_then_itIsTheOnlyLineOfItsInfo() = runTuIndiceUiTest {
		var info: RichTooltipText? = null

		setTuIndiceTestContent {
			info = UiText.Raw("Cada barra cuenta las notas.").toBlockInfo()
		}
		waitForIdle()

		assertEquals(RichTooltipText(lines = listOf("Cada barra cuenta las notas.")), info)
	}
}
