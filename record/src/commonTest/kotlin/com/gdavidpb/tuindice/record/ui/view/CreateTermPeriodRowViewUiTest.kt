package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermPeriodRowViewUiTest {
	@Test
	fun when_theRowIsDrawn_then_thePeriodAndTheLoadEachSitUnderTheirLabel_withTheMenuClosed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PeriodRow(hasSelectedSubjects = false, onPeriodSelected = {})
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermPeriodSelector).assertTextEquals("Ene - Mar 2027")
		onNodeWithText("Carga").assertIsDisplayed()
		onNodeWithText("Sin materias").assertIsDisplayed()
		// The options stay out of the tree until the selector is opened.
		onAllNodesWithTag(RecordUiTags.createSyntheticTermPeriodOption(AprJul.termKey)).assertCountEquals(0)

		val periodLabel = onNodeWithText("Trimestre").assertIsDisplayed().getUnclippedBoundsInRoot()
		val selector = onNodeWithTag(RecordUiTags.CreateSyntheticTermPeriodSelector).getUnclippedBoundsInRoot()
		val loadLabel = onNodeWithText("Carga").getUnclippedBoundsInRoot()

		assertTrue(periodLabel.bottom <= selector.top, "the label names the selector under it")
		assertTrue(selector.right <= loadLabel.left, "the load sits beside the period, not under it")
	}

	@Test
	fun when_theSelectorIsOpened_andAPeriodIsChosen_then_itsKeyIsReported_andTheMenuCloses() = runTuIndiceUiTest {
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			PeriodRow(hasSelectedSubjects = false, onPeriodSelected = { termKey -> reported += termKey })
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermPeriodSelector).performClick()

		assertNodeVisible(RecordUiTags.createSyntheticTermPeriodOption(JanMar.termKey))
		assertNodeVisible(RecordUiTags.createSyntheticTermPeriodOption(AprJul.termKey))
		onNodeWithTag(RecordUiTags.createSyntheticTermPeriodOption(AprJul.termKey))
			.assertTextEquals("Abr - Jul 2027")
			.performClick()

		assertEquals(listOf("2027-APR_JUL"), reported)
		waitUntil(timeoutMillis = 5_000) {
			onAllNodesWithTag(RecordUiTags.createSyntheticTermPeriodOption(AprJul.termKey))
				.fetchSemanticsNodes()
				.isEmpty()
		}
		// The row shows the period it is handed: choosing another is the caller's to apply.
		onNodeWithTag(RecordUiTags.CreateSyntheticTermPeriodSelector).assertTextEquals("Ene - Mar 2027")
	}

	@Test
	fun when_subjectsAreSelectedAndTheLoadIsEstimated_then_theChipBesideThePeriodNamesTheBand() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PeriodRow(hasSelectedSubjects = true, onPeriodSelected = {})
		}

		onNodeWithText("Exigente").assertIsDisplayed()
		onAllNodesWithText("Sin materias").assertCountEquals(0)
	}

	@Composable
	private fun PeriodRow(hasSelectedSubjects: Boolean, onPeriodSelected: (String) -> Unit) {
		CreateTermPeriodRow(
			selectedPeriod = JanMar,
			periodOptions = listOf(JanMar, AprJul),
			loadPreview = SyntheticTermLoadPreview(available = true, band = SyntheticTermLoadBand.DEMANDING),
			hasSelectedSubjects = hasSelectedSubjects,
			isLoadingLoadPreview = false,
			hasLoadPreviewError = false,
			onPeriodSelected = onPeriodSelected
		)
	}

	private companion object {
		val JanMar = SyntheticTermPeriodOption(periodYear = 2027, periodCode = AcademicTermPeriod.JAN_MAR)
		val AprJul = SyntheticTermPeriodOption(periodYear = 2027, periodCode = AcademicTermPeriod.APR_JUL)
	}
}
