package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectDetailSegmentTabsViewUiTest {
	@Test
	fun when_careerTabSelected_then_onlyCareerChipIsSelected() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailSegmentTabsView(
				selectedTab = SubjectSegmentTab.CAREER,
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.CareerTab)
			.assertTextEquals("Tu carrera")
			.assertIsSelected()
		onNodeWithTag(SubjectsUiTags.GlobalTab)
			.assertTextEquals("General")
			.assertIsNotSelected()
	}

	@Test
	fun when_globalTabSelected_then_onlyGlobalChipIsSelected() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailSegmentTabsView(
				selectedTab = SubjectSegmentTab.GLOBAL,
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.CareerTab).assertIsNotSelected()
		onNodeWithTag(SubjectsUiTags.GlobalTab).assertIsSelected()
	}

	@Test
	fun when_tabsTapped_then_forwardsTheTappedTab() = runTuIndiceUiTest {
		val selections = mutableListOf<SubjectSegmentTab>()

		setTuIndiceTestContent {
			SubjectDetailSegmentTabsView(
				selectedTab = SubjectSegmentTab.CAREER,
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = { tab -> selections += tab }
			)
		}

		onNodeWithTag(SubjectsUiTags.GlobalTab).performClick()
		onNodeWithTag(SubjectsUiTags.CareerTab).performClick()

		assertEquals(
			listOf(SubjectSegmentTab.GLOBAL, SubjectSegmentTab.CAREER),
			selections
		)
	}
}
