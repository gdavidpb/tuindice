package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.summary.ui.chart.waitUntilAbsentText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummarySectionInfoViewUiTest {
	@Test
	fun when_theMarkIsTapped_then_itsTooltipShowsTheExplanationAndItIsNamedForAScreenReader() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			InfoHost()
		}
		waitForIdle()

		onNodeWithTag(TAG)
			.assertIsDisplayed()
			.assertContentDescriptionEquals("Qué muestra este bloque")
		onNodeWithTag(TAG).performClick()

		assertTooltipShows(EXPLANATION)
	}

	@Test
	fun when_theMarkIsTappedAgain_then_itsTooltipCloses() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			InfoHost()
		}
		waitForIdle()

		onNodeWithTag(TAG).performClick()
		assertTooltipShows(EXPLANATION)
		onNodeWithTag(TAG).performClick()

		waitUntilAbsentText(EXPLANATION)
	}

	@Composable
	private fun InfoHost() {
		SummarySectionInfoView(
			info = RichTooltipText(lines = listOf(EXPLANATION)),
			tag = TAG,
			modifier = Modifier
		)
	}

	private companion object {
		const val TAG = "summary_section_info_test"
		const val EXPLANATION = "Explica lo que muestra el bloque."
	}
}
