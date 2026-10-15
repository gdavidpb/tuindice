package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsCreditsViewUiTest {
	@Test
	fun when_aPensumIsCached_then_theFigureAndItsShareAreWrittenAndOneBarFollowsThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsCreditsView(item = creditsWith(content = pensum()))
		}

		onNodeWithText("152").assertIsDisplayed()
		onNodeWithText("de 205 créditos").assertIsDisplayed()
		onNodeWithText("74 %").assertIsDisplayed()
		onAllNodesWithTag(SummaryUiTags.StatisticsPensumTrack, useUnmergedTree = true).assertCountEquals(1)
	}

	@Test
	fun when_aPensumIsCached_then_theFigureDoesNotSayWhereItComesFrom() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsCreditsView(item = creditsWith(content = pensum()))
		}

		onAllNodesWithText("pensum", substring = true, ignoreCase = true).assertCountEquals(0)
		onAllNodesWithText("registro", substring = true, ignoreCase = true).assertCountEquals(0)
	}

	@Test
	fun when_noPensumIsCached_then_theInvitationIsWrittenAndNoBarIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsCreditsView(item = creditsWith(content = invitation()))
		}

		onNodeWithText("Abre tu pensum para ver tus créditos.").assertIsDisplayed()
		onAllNodesWithTag(SummaryUiTags.StatisticsPensumTrack, useUnmergedTree = true).assertCountEquals(0)
	}

	private fun creditsWith(content: SummaryStatisticsCreditsItem.Content): SummaryStatisticsCreditsItem {
		return SummaryStatisticsCreditsItem(
			title = words("Créditos"),
			content = content
		)
	}

	private fun pensum(): SummaryStatisticsCreditsItem.Content.Data {
		return SummaryStatisticsCreditsItem.Content.Data(
			figure = figureOf(value = "152", label = "de 205 créditos", trailing = "74 %"),
			fraction = 0.74f,
			description = words("Avance del pensum: 152 de 205 créditos, 74 %")
		)
	}

	private fun invitation(): SummaryStatisticsCreditsItem.Content.Empty {
		return SummaryStatisticsCreditsItem.Content.Empty(text = words("Abre tu pensum para ver tus créditos."))
	}
}
