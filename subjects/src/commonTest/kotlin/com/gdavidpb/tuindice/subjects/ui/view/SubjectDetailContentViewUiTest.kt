package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.testing.globalSubjectSegmentItem
import com.gdavidpb.tuindice.subjects.testing.subjectDetailItem
import com.gdavidpb.tuindice.subjects.testing.subjectSegmentItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectDetailContentViewUiTest {
	@Test
	fun when_detailHasSingleSegment_then_hidesTabsAndDisplaysItsSummary() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailContentView(
				detail = subjectDetailItem().copy(
					hasSegmentTabs = false,
					generatedAtText = "Actualizado 09/03/2024"
				),
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {}
			)
		}

		assertNodeVisible(SubjectsUiTags.Content)
		assertNodeHidden(SubjectsUiTags.CareerTab)
		assertNodeHidden(SubjectsUiTags.GlobalTab)
		onNodeWithText("Calculo I").assertIsDisplayed()
		onNodeWithTag(SubjectsUiTags.SegmentStudentsMetric)
			.assertContentDescriptionEquals("18 estudiantes que cursaron esta materia")
		onNodeWithTag(SubjectsUiTags.GeneratedAt)
			.assertTextEquals("Actualizado 09/03/2024")
	}

	@Test
	fun when_detailHasSegmentTabs_then_displaysTabsAndForwardsSelection() = runTuIndiceUiTest {
		val selections = mutableListOf<SubjectSegmentTab>()

		setTuIndiceTestContent {
			SubjectDetailContentView(
				detail = subjectDetailItem().copy(
					selectedTab = SubjectSegmentTab.CAREER,
					hasSegmentTabs = true,
					globalSegment = globalSubjectSegmentItem()
				),
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = { tab -> selections += tab }
			)
		}

		onNodeWithTag(SubjectsUiTags.CareerTab)
			.assertTextEquals("Tu carrera")
			.assertIsSelected()
		onNodeWithTag(SubjectsUiTags.GlobalTab)
			.assertTextEquals("General")
			.performClick()

		assertEquals(listOf(SubjectSegmentTab.GLOBAL), selections)
	}

	@Test
	fun when_globalTabIsSelected_then_displaysGlobalSegmentMetrics() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailContentView(
				detail = subjectDetailItem().copy(
					selectedTab = SubjectSegmentTab.GLOBAL,
					hasSegmentTabs = true,
					careerSegment = subjectSegmentItem(),
					globalSegment = globalSubjectSegmentItem()
				),
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.GlobalTab).assertIsSelected()
		onNodeWithTag(SubjectsUiTags.SegmentStudentsMetric)
			.assertContentDescriptionEquals("2.4k estudiantes que cursaron esta materia")
		onNodeWithTag(SubjectsUiTags.SegmentAttemptsMetric)
			.assertContentDescriptionEquals("3.7k veces que fue cursada esta materia")
		onNodeWithText("67 / 100").assertExists()
	}

	@Test
	fun when_selectedSegmentIsMissing_then_fallsBackToTheAvailableSegment() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailContentView(
				detail = subjectDetailItem().copy(
					selectedTab = SubjectSegmentTab.CAREER,
					careerSegment = null,
					globalSegment = globalSubjectSegmentItem()
				),
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {}
			)
		}

		assertNodeVisible(SubjectsUiTags.Content)
		onNodeWithTag(SubjectsUiTags.SegmentStudentsMetric)
			.assertContentDescriptionEquals("2.4k estudiantes que cursaron esta materia")
	}

	@Test
	fun when_detailHasNoSegments_then_rendersNothing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailContentView(
				detail = subjectDetailItem().copy(
					careerSegment = null,
					globalSegment = null
				),
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {}
			)
		}

		assertNodeHidden(SubjectsUiTags.Content)
		assertNodeHidden(SubjectsUiTags.Charts)
		assertNodeHidden(SubjectsUiTags.GeneratedAt)
	}

	@Test
	fun when_scrolledToTheBottom_then_reportsChartsBecameVisible() = runTuIndiceUiTest {
		val visibilityChanges = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			SubjectDetailContentView(
				detail = subjectDetailItem(),
				careerTabText = "Tu carrera",
				globalTabText = "General",
				onTabSelected = {},
				onChartsVisibilityChange = { isVisible -> visibilityChanges += isVisible }
			)
		}

		waitForIdle()
		assertEquals(emptyList(), visibilityChanges)

		onNodeWithTag(SubjectsUiTags.GeneratedAt).performScrollTo()

		waitUntil(timeoutMillis = 5_000) {
			visibilityChanges.isNotEmpty()
		}

		assertEquals(listOf(true), visibilityChanges)
	}
}
