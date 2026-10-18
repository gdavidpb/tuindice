package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermReportItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryTermReportViewUiTest {

	@Test
	fun when_aReportIsUnread_then_theLineNamesItsTermAndLeadsToIt() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryHubView(
				hub = SummaryHubItem(termReport = unreadReportLine()),
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		assertNodeVisible(TermReportUiTags.TermReportEntry)
		onNodeWithText("Tu informe de Abr - Jul 2026").assertIsDisplayed()
		onNodeWithText("Cerraste con 4,18").assertIsDisplayed()

		onNodeWithTag(TermReportUiTags.TermReportEntry).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.TermReport(insightId = "term-1:2")), targets)
	}

	@Test
	fun when_everyReportWasOpened_then_theLineIsNotShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryHubView(hub = SummaryHubItem(), actions = SummaryHubActions())
		}

		onNodeWithTag(TermReportUiTags.TermReportEntry).assertDoesNotExist()
	}

	private fun unreadReportLine() = SummaryTermReportItem(
		insightId = "term-1:2",
		title = UiText.Raw("Tu informe de Abr - Jul 2026"),
		subtitle = "Cerraste con 4,18",
		target = SummaryHubTarget.TermReport(insightId = "term-1:2")
	)
}
