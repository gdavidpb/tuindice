package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsOverdueRowUiTest {
	@Test
	fun when_evaluationsAreOverdue_then_theirCountIsWrittenInTheLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			OverdueHost()
		}
		waitForIdle()

		onNodeWithText("2 vencidas sin nota").assertIsDisplayed()
	}

	@Test
	fun when_theOldestWeekIsUnknown_then_theLineHasNoArrowToTap() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			OverdueHost()
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsOverdue).assertHasNoClickAction()
	}

	@Composable
	private fun OverdueHost() {
		Column {
			SummaryStatisticsOverdueRow(
				overdue = SummaryStatisticsCurrentTermItem.Overdue(text = words("2 vencidas sin nota"), target = null),
				actions = SummaryHubActions()
			)
		}
	}
}
