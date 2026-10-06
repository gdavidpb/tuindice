package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermAddSubjectTabsViewUiTest {
	@Test
	fun when_suggestedIsTheChosenTab_then_itIsSelected_andBothTabsAreNamed_suggestedFirst() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermAddSubjectTabs(
				selectedTab = CreateTermAddSubjectTab.Suggested,
				onTabSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSuggestedTab)
			.assertTextEquals("Sugeridas")
			.assertIsSelected()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab)
			.assertTextEquals("Buscar")
			.assertIsNotSelected()

		val suggested = onNodeWithTag(RecordUiTags.CreateSyntheticTermSuggestedTab).getUnclippedBoundsInRoot()
		val search = onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).getUnclippedBoundsInRoot()

		assertTrue(suggested.left < search.left, "what the pensum suggests comes before searching")
	}

	@Test
	fun when_searchIsTheChosenTab_then_theSelectionSitsOnIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermAddSubjectTabs(
				selectedTab = CreateTermAddSubjectTab.Search,
				onTabSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).assertIsSelected()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSuggestedTab).assertIsNotSelected()
	}

	@Test
	fun when_aTabIsTapped_then_itIsReported_andTheSelectionStaysWithTheCaller() = runTuIndiceUiTest {
		val reported = mutableListOf<CreateTermAddSubjectTab>()

		setTuIndiceTestContent {
			CreateTermAddSubjectTabs(
				selectedTab = CreateTermAddSubjectTab.Suggested,
				onTabSelected = { tab -> reported += tab }
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSuggestedTab).performClick()

		assertEquals(listOf(CreateTermAddSubjectTab.Search, CreateTermAddSubjectTab.Suggested), reported)
		// The tabs keep no selection of their own: until the caller hands the new tab back, the one
		// they were given stays selected.
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSuggestedTab).assertIsSelected()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).assertIsNotSelected()
	}
}
