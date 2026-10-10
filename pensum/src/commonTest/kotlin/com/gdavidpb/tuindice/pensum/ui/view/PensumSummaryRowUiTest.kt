package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_PENSUM_CAREER_NAME
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModel
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModelWithSelectableYears
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumSummaryRowUiTest {
	@Test
	fun when_summaryIsExpanded_then_showsCareerProgressCreditsAndPensumContext() = runTuIndiceUiTest {
		val model = samplePensumScreenModelWithSelectableYears().copy(
			progressPercent = 40,
			approvedCredits = 32,
			totalCredits = 80
		)

		setTuIndiceTestContent {
			PensumSummaryRow(model = model, onPensumContextClick = {})
		}
		waitForIdle()

		onNodeWithText(SAMPLE_PENSUM_CAREER_NAME).assertExists()
		onNodeWithText("40% Avance").assertExists()
		onNodeWithText("32 / 80 UC").assertExists()
		onNodeWithText("Pensum 2019").assertExists()
		onNodeWithText("Proyecto de Grado").assertExists()
	}

	@Test
	fun when_selectedModalityIsNotAmongTheOptions_then_showsItsIdInstead() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSummaryRow(model = samplePensumScreenModel(), onPensumContextClick = {})
		}

		onNodeWithText("Pensum 2019").assertExists()
		onNodeWithText("degree_project").assertExists()
	}

	@Test
	fun when_summaryIsCollapsed_then_onlyTheCareerHeaderRemains() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSummaryRow(
				model = samplePensumScreenModelWithSelectableYears(),
				isCollapsed = true,
				onPensumContextClick = {}
			)
		}

		assertNodeVisible(PensumUiTags.PensumSummaryContainer)
		onNodeWithText(SAMPLE_PENSUM_CAREER_NAME).assertExists()
		assertNodeHidden(PensumUiTags.PensumContextSummary)
		onNodeWithText("Pensum 2019").assertDoesNotExist()
		onNodeWithText("0% Avance").assertDoesNotExist()
	}

	@Test
	fun when_careerNameIsBlank_then_headerIsOmittedButContextStaysAvailable() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSummaryRow(
				model = samplePensumScreenModelWithSelectableYears().copy(careerName = ""),
				onPensumContextClick = {}
			)
		}

		assertNodeHidden(PensumUiTags.PensumSummaryContainer)
		assertNodeVisible(PensumUiTags.PensumContextSummary)
		onNodeWithText("Pensum 2019").assertExists()
	}

	@Test
	fun when_headerAndContextAreTapped_then_eachInvokesItsOwnCallback() = runTuIndiceUiTest {
		val events = mutableListOf<String>()

		setTuIndiceTestContent {
			PensumSummaryRow(
				model = samplePensumScreenModelWithSelectableYears(),
				onSummaryClick = { events += "summary" },
				onPensumContextClick = { events += "context" }
			)
		}

		onNodeWithTag(PensumUiTags.PensumSummaryContainer).assertHasClickAction().performClick()
		onNodeWithTag(PensumUiTags.PensumContextSummary).assertHasClickAction().performClick()

		runOnIdle { assertEquals(listOf("summary", "context"), events) }
	}
}
