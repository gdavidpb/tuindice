package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ApprovedSearchResultsToggleViewUiTest {
	@Test
	fun when_theTakenSubjectsAreHidden_then_theToggleOffersToShowThem_withTheirCount() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ApprovedSearchResultsToggle(count = 3, isExpanded = false, onClick = {})
		}

		assertNodeVisible(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle)
			.assertTextEquals("Mostrar 3 ya cursadas")
			.assertHasClickAction()
		onAllNodesWithText("Ocultar", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_theTakenSubjectsAreShown_then_theToggleOffersToHideThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ApprovedSearchResultsToggle(count = 12, isExpanded = true, onClick = {})
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).assertTextEquals("Ocultar 12 ya cursadas")
		onAllNodesWithText("Mostrar", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_theToggleIsTapped_then_theTapIsReported_andWhatItSaysStaysWithTheCaller() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			ApprovedSearchResultsToggle(count = 3, isExpanded = false, onClick = { clicks++ })
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		assertEquals(1, clicks)
		// It keeps no state of its own: until the caller expands the list, it still offers to show it.
		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).assertTextEquals("Mostrar 3 ya cursadas")
	}
}
