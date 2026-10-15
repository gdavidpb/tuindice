package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsCreditsViewUiTest {
	@Test
	fun when_aPensumIsCached_then_itsProgressFollowsTheBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsCreditsView(item = creditsWith(pensum = pensum()), chartActionLabel = "Ver detalle")
		}

		onNodeWithText("164").assertIsDisplayed()
		onNodeWithText("de 205 según el pensum").assertIsDisplayed()
		onNodeWithText("74 %").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.StatisticsPensumTrack).assertIsDisplayed()
	}

	@Test
	fun when_noPensumIsCached_then_nothingFollowsTheBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsCreditsView(item = creditsWith(pensum = null), chartActionLabel = "Ver detalle")
		}

		onNodeWithText("152 aprobados").assertIsDisplayed()
		onAllNodesWithTag(SummaryUiTags.StatisticsPensumTrack).assertCountEquals(0)
	}

	private fun creditsWith(pensum: SummaryStatisticsCreditsItem.Pensum?): SummaryStatisticsCreditsItem {
		return SummaryStatisticsCreditsItem(
			title = words("Créditos"),
			content = SummaryStatisticsCreditsItem.Content.Data(
				figure = figureOf(value = "164", label = "créditos cursados según tu registro"),
				segments = listOf(
					segmentOf(SummaryStatisticsOutcome.APPROVED, 152f, "152 aprobados", "152 aprobados · 93 %"),
					segmentOf(SummaryStatisticsOutcome.FAILED, 8f, "8 reprobados", "8 reprobados · 5 %"),
					segmentOf(SummaryStatisticsOutcome.WITHDRAWN, 4f, "4 retirados", "4 retirados · 2 %")
				),
				pensum = pensum,
				chartSummary = words("164 créditos cursados según tu registro")
			)
		)
	}

	private fun pensum(): SummaryStatisticsCreditsItem.Pensum {
		return SummaryStatisticsCreditsItem.Pensum(
			figure = figureOf(value = "152", label = "de 205 según el pensum", trailing = "74 %"),
			fraction = 0.74f
		)
	}
}
