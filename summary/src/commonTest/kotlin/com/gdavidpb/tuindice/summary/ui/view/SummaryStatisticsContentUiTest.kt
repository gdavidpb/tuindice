package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryStatisticsItem
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsContentUiTest {
	@Test
	fun when_contentIsGiven_then_itsSixBlocksAreWrittenInTheirOrder() = runTuIndiceUiTest {
		val statistics = statisticsHubOf(statistics = statisticsOf()).toSummaryStatisticsItem()!!

		setTuIndiceTestContent {
			SummaryStatisticsContent(statistics = statistics, actions = SummaryHubActions())
		}
		waitForIdle()

		// The blocks are the children of the column, in the order the screen lays them out.
		val blocks = onNodeWithTag(SummaryUiTags.StatisticsContent, useUnmergedTree = true)
			.onChildren()
			.fetchSemanticsNodes()
			.map { node -> node.config.getOrNull(SemanticsProperties.TestTag) }

		assertEquals(
			listOf(
				SummaryUiTags.StatisticsCurrentTermBlock,
				SummaryUiTags.StatisticsIndexBlock,
				SummaryUiTags.StatisticsSubjectsBlock,
				SummaryUiTags.StatisticsCreditsBlock,
				SummaryUiTags.StatisticsGradesBlock,
				SummaryUiTags.StatisticsPaceBlock
			),
			blocks
		)
	}

	@Test
	fun when_contentIsLongerThanTheScreen_then_itScrollsWithItsBlocks() = runTuIndiceUiTest {
		val statistics = statisticsHubOf(statistics = statisticsOf()).toSummaryStatisticsItem()!!

		setTuIndiceTestContent {
			SummaryStatisticsContent(statistics = statistics, actions = SummaryHubActions())
		}
		waitForIdle()

		val config = onNodeWithTag(SummaryUiTags.StatisticsContent, useUnmergedTree = true).fetchSemanticsNode().config
		assertTrue(config.contains(SemanticsProperties.VerticalScrollAxisRange))
	}
}
