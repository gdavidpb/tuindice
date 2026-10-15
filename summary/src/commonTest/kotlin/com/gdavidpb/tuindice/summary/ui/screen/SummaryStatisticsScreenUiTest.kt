package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryStatisticsItem
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsScreenUiTest {
	@Test
	fun when_loading_then_theProgressIndicatorShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsScreen(
				state = SummaryStatistics.State.Loading,
				actions = SummaryHubActions()
			)
		}

		assertNodeVisible(SummaryUiTags.LoadingIndicator)
	}

	@Test
	fun when_unavailable_then_itsMessageIsWrittenAndNothingCanBeRetried() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsScreen(
				state = SummaryStatistics.State.Unavailable(
					title = UiText.Raw("Estadísticas no disponibles"),
					message = UiText.Raw("No pudimos leer tu registro en este momento.")
				),
				actions = SummaryHubActions()
			)
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsUnavailableTitle).assertTextEquals("Estadísticas no disponibles")
		onNodeWithTag(SummaryUiTags.StatisticsUnavailableMessage)
			.assertTextEquals("No pudimos leer tu registro en este momento.")
		onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick), useUnmergedTree = true)
			.assertCountEquals(0)
	}

	@Test
	fun when_content_then_itsSixBlocksAreWrittenInTheirOrder() = runTuIndiceUiTest {
		val statistics = statisticsHubOf(statistics = statisticsOf()).toSummaryStatisticsItem()!!

		setTuIndiceTestContent {
			SummaryStatisticsScreen(
				state = SummaryStatistics.State.Content(statistics = statistics),
				actions = SummaryHubActions()
			)
		}
		waitForIdle()

		// The blocks are the children of the content, in the order the screen lays them out.
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
	fun when_idle_then_noContentIsWrittenAndTheRootPaintsAnOpaqueBackground() = runTuIndiceUiTest {
		var background = Color.Unspecified

		setTuIndiceTestContent {
			background = MaterialTheme.colorScheme.background

			SummaryStatisticsScreen(
				state = SummaryStatistics.State.Idle,
				actions = SummaryHubActions(),
				modifier = Modifier.testTag(ROOT_TAG)
			)
		}
		waitForIdle()

		onAllNodesWithTag(SummaryUiTags.StatisticsContent).assertCountEquals(0)
		onAllNodesWithTag(SummaryUiTags.StatisticsUnavailable).assertCountEquals(0)
		onAllNodesWithTag(SummaryUiTags.LoadingIndicator).assertCountEquals(0)

		// The root has no content to show, so the pixel at its centre is its own background, and it is opaque.
		val pixels = onNodeWithTag(ROOT_TAG).captureToImage().toPixelMap()
		val pixel = pixels[pixels.width / 2, pixels.height / 2]

		assertEquals(1f, pixel.alpha, COLOR_TOLERANCE)
		assertEquals(background.red, pixel.red, COLOR_TOLERANCE)
		assertEquals(background.green, pixel.green, COLOR_TOLERANCE)
		assertEquals(background.blue, pixel.blue, COLOR_TOLERANCE)
	}
}

private const val ROOT_TAG = "summary_statistics_screen_root"
private const val COLOR_TOLERANCE = 0.05f
