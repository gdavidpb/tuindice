package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.testing.termItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TermSelectionBottomSheetUiTest {
	@Test
	fun when_termsSpanSeveralYears_then_eachYearHeadsItsTerms_newestFirst() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectionBottomSheet(
				// Handed over out of order: the sheet sorts them itself.
				terms = listOf(historical2024(), current2026(), synthetic2026()),
				selectedTermId = "current-2026",
				viewMode = RecordViewMode.Projection,
				onTermSelected = {},
				onDismissRequest = {}
			)
		}

		awaitSheet()

		onNodeWithText("Todos los trimestres").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.TermSelectionList).assertIsDisplayed()

		val tops = listOf(
			RecordUiTags.termSelectionYear(2026),
			RecordUiTags.termSelectionOption("current-2026"),
			RecordUiTags.termSelectionOption("synthetic-2026"),
			RecordUiTags.termSelectionYear(2024),
			RecordUiTags.termSelectionOption("historical-2024")
		).map { tag -> onNodeWithTag(tag).assertIsDisplayed().getUnclippedBoundsInRoot().top }

		assertEquals(tops.sorted(), tops, "years go from the newest down, and so do the terms of a year")
		assertTrue(tops.distinct().size == tops.size, "every header and every term has a line of its own")
	}

	@Test
	fun when_theRecordIsInProjection_then_theSheetSaysWhatThatModeLists() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectionBottomSheet(
				terms = listOf(current2026()),
				selectedTermId = "current-2026",
				viewMode = RecordViewMode.Projection,
				onTermSelected = {},
				onDismissRequest = {}
			)
		}

		awaitSheet()

		onNodeWithText("Proyección: trimestre actual y simulaciones").assertIsDisplayed()
		assertEquals(0, onAllNodesWithTag(RecordUiTags.termSelectionYear(2024)).fetchSemanticsNodes().size)
	}

	@Test
	fun when_theRecordIsHistorical_then_theSheetSaysOnlyClosedTermsAreListed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectionBottomSheet(
				terms = listOf(historical2024()),
				selectedTermId = "historical-2024",
				viewMode = RecordViewMode.Historical,
				onTermSelected = {},
				onDismissRequest = {}
			)
		}

		awaitSheet()

		onNodeWithText("Histórico: solo trimestres cerrados").assertIsDisplayed()
	}

	@Test
	fun when_aTermIsTheSelectedOne_then_onlyItsRowCarriesTheCheck() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectionBottomSheet(
				terms = listOf(current2026(), synthetic2026(), historical2024()),
				selectedTermId = "synthetic-2026",
				viewMode = RecordViewMode.Projection,
				onTermSelected = {},
				onDismissRequest = {}
			)
		}

		awaitSheet()

		onNodeWithTag(RecordUiTags.termSelectionSelectedIcon("synthetic-2026"), useUnmergedTree = true)
			.assertIsDisplayed()
		assertNodeHidden(RecordUiTags.termSelectionSelectedIcon("current-2026"), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.termSelectionSelectedIcon("historical-2024"), useUnmergedTree = true)
	}

	@Test
	fun when_aTermIsTapped_then_itIsReported_andTheSheetAsksToClose() = runTuIndiceUiTest {
		val selected = mutableListOf<String>()
		var dismissRequests = 0

		setTuIndiceTestContent {
			TermSelectionBottomSheet(
				terms = listOf(current2026(), historical2024()),
				selectedTermId = "current-2026",
				viewMode = RecordViewMode.Projection,
				onTermSelected = { termId -> selected += termId },
				onDismissRequest = { dismissRequests++ }
			)
		}

		awaitSheet()

		onNodeWithTag(RecordUiTags.termSelectionOption("historical-2024")).performClick()

		assertEquals(listOf("historical-2024"), selected)
		assertEquals(1, dismissRequests)
	}

	// The sheet is a popup: its nodes arrive a frame after the content that asked for it.
	private fun ComposeUiTest.awaitSheet() {
		waitUntil(timeoutMillis = 5_000) {
			onAllNodesWithTag(RecordUiTags.TermSelectionSheet).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun current2026(): TermItem = termItem(
		termId = "current-2026",
		shortNameText = "Sep - Dic 2026",
		kind = TermItemKind.CURRENT
	).copy(periodYear = 2026, termOrder = 20266)

	private fun synthetic2026(): TermItem = termItem(
		termId = "synthetic-2026",
		shortNameText = "Abr - Jul 2026",
		kind = TermItemKind.SYNTHETIC
	).copy(periodYear = 2026, termOrder = 20263)

	private fun historical2024(): TermItem = termItem(
		termId = "historical-2024",
		shortNameText = "Ene - Mar 2024",
		kind = TermItemKind.HISTORICAL
	).copy(periodYear = 2024, termOrder = 20241)
}
