package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsBlockViewUiTest {
	@Test
	fun when_aLinkIsGiven_then_itIsWrittenAndTakesTheStudentToItsTarget() = runTuIndiceUiTest {
		var linked = false

		setTuIndiceTestContent {
			BlockHost(
				linkText = "Sep - Dic 2026",
				onLinkClick = { linked = true }
			)
		}

		onNodeWithText("Sep - Dic 2026").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.sectionLink(BlockTag)).performClick()

		assertTrue(linked)
	}

	@Test
	fun when_noLinkIsGiven_then_nothingIsWrittenAtTheHead_andTheContentStays() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BlockHost(linkText = null, onLinkClick = {})
		}

		onAllNodesWithTag(SummaryUiTags.sectionLink(BlockTag)).assertCountEquals(0)
		onNodeWithText("Contenido del bloque").assertIsDisplayed()
	}

	@Test
	fun when_anExplanationIsGiven_then_itsMarkIsBesideTheTitleAndOpensTheExplanation() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BlockHost(
				linkText = null,
				onLinkClick = {},
				info = RichTooltipText(lines = listOf(EXPLANATION))
			)
		}

		onNodeWithTag(SummarySectionUiTags.info(BlockTag)).assertIsDisplayed()
		onNodeWithTag(SummarySectionUiTags.info(BlockTag)).performClick()

		assertTooltipShows(EXPLANATION)
	}

	@Test
	fun when_noExplanationIsGiven_then_noMarkIsWrittenAtTheHead() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BlockHost(linkText = null, onLinkClick = {})
		}

		onAllNodesWithTag(SummarySectionUiTags.info(BlockTag)).assertCountEquals(0)
	}

	@Composable
	private fun BlockHost(
		linkText: String?,
		onLinkClick: () -> Unit,
		info: RichTooltipText? = null
	) {
		SummaryStatisticsBlockView(
			head = SummaryStatisticsBlockHead(
				tag = BlockTag,
				title = "Este trimestre",
				linkText = linkText,
				onLinkClick = onLinkClick,
				info = info
			)
		) {
			SummaryStatisticsEmptyView(message = "Contenido del bloque")
		}
	}

	private companion object {
		const val BlockTag = "summary_statistics_test_block"
		const val EXPLANATION = "Explica lo que muestra el bloque."
	}
}
