package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import com.gdavidpb.tuindice.record.testing.termItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TermSummaryViewUiTest {
	@Test
	fun when_aTermIsShown_then_itsSummaryNamesTheThreeMetrics_insetFromTheEdgesOfThePage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSummaryView(
				modifier = Modifier.testTag(RecordUiTags.SelectedTermSummary),
				item = termItem()
			)
		}

		assertNodeVisible(RecordUiTags.SelectedTermSummary)
		onNodeWithText("Δx 4.0000").assertIsDisplayed()
		onNodeWithText("∑x 3.5000").assertIsDisplayed()
		onNodeWithText("⦿ 12").assertIsDisplayed()

		val summary = onNodeWithTag(RecordUiTags.SelectedTermSummary).getUnclippedBoundsInRoot()
		val first = onNodeWithText("Trimestre").assertIsDisplayed().getUnclippedBoundsInRoot()
		val last = onNodeWithText("UC inscritas").assertIsDisplayed().getUnclippedBoundsInRoot()

		// The summary spans the page; what it says keeps the page's 16dp margin on both sides.
		assertTrue(first.left - summary.left >= 16.dp, "the first metric is inset from the start")
		assertTrue(summary.right - last.right >= 16.dp, "the last metric is inset from the end")
	}

	@Test
	fun when_theTermChangesUnderTheSummary_then_itShowsTheNewTermsNumbers_andChanges() = runTuIndiceUiTest {
		var item by mutableStateOf(termItem())

		setTuIndiceTestContent {
			TermSummaryView(item = item)
		}

		onNodeWithText("Δx 4.0000").assertIsDisplayed()
		onAllNodesWithText("▲", substring = true).assertCountEquals(0)

		// Paging to a newer term: the same view is handed another item.
		item = termItem().copy(
			gradeText = AnnotatedString("Δx 4.5000"),
			gradeDelta = TermMetricDelta(text = "▲ 0.5000", tone = TermMetricDeltaTone.Positive)
		)
		waitForIdle()

		onNodeWithText("Δx 4.5000").assertIsDisplayed()
		onNodeWithText("▲ 0.5000").assertIsDisplayed()
		onAllNodesWithText("Δx 4.0000").assertCountEquals(0)
	}
}
