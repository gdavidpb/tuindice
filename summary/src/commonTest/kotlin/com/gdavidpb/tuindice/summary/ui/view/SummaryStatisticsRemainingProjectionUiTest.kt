package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsRemainingProjectionUiTest {
	@Test
	fun when_aCountIsGiven_then_ItIsWrittenAsAFigure() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RemainingHost(count = "6")
		}
		waitForIdle()

		onNodeWithText("6").assertIsDisplayed()
		onNodeWithText("trimestres").assertIsDisplayed()
	}

	@Test
	fun when_aCountIsGiven_then_ItsTagIsBesideIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RemainingHost(count = "6")
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsProjection)
		onNodeWithText("Estimado").assertIsDisplayed()
	}

	@Composable
	private fun RemainingHost(count: String) {
		Column {
			SummaryStatisticsRemainingProjection(
				projection = SummaryStatisticsPaceItem.Projection.Remaining(
					fact = SummaryStatisticsFactItem(value = count, label = words("trimestres")),
					tag = words("Estimado")
				)
			)
		}
	}
}
