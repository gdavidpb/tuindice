package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModelWithSelectableYears
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumSelectionBottomSheetUiTest {
	@Test
	fun when_sheetOpens_then_marksTheCurrentYearAndModality() = runTuIndiceUiTest {
		setSelectionSheetContent(SelectionSheetRecorder())

		onNodeWithText("Cambiar pensum").assertExists()
		assertNodeVisible(PensumUiTags.PensumCurrentSelectionSummary)
		onNodeWithText("Versión del pensum").assertExists()
		onNodeWithText("Modalidad").assertExists()
		onNodeWithTag(PensumUiTags.versionOption(year = 2019)).assertIsSelected()
		onNodeWithTag(PensumUiTags.versionOption(year = 2018)).assertIsNotSelected()
		onNodeWithTag(PensumUiTags.modalityOption("degree_project")).assertIsSelected()
		onNodeWithTag(PensumUiTags.modalityOption("long_internship")).assertIsNotSelected()
	}

	@Test
	fun when_selectionIsUnchangedAndApplied_then_onlyDismissIsRequested() = runTuIndiceUiTest {
		val recorder = SelectionSheetRecorder()
		setSelectionSheetContent(recorder)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { recorder.dismissCount == 1 }

		assertEquals(emptyList(), recorder.appliedSelections)
	}

	@Test
	fun when_anotherModalityIsChosenAndApplied_then_reportsItForTheCurrentYear() = runTuIndiceUiTest {
		val recorder = SelectionSheetRecorder()
		setSelectionSheetContent(recorder)

		onNodeWithTag(PensumUiTags.modalityOption("long_internship")).performClick()
		onNodeWithTag(PensumUiTags.modalityOption("long_internship")).assertIsSelected()
		onNodeWithTag(PensumUiTags.modalityOption("degree_project")).assertIsNotSelected()
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { recorder.dismissCount == 1 }

		assertEquals(listOf(2019 to "long_internship"), recorder.appliedSelections)
	}

	@Test
	fun when_yearWithoutTheChosenModalityIsPicked_then_modalityFallsBackToItsDefault() =
		runTuIndiceUiTest {
			val recorder = SelectionSheetRecorder()
			setSelectionSheetContent(recorder)

			onNodeWithTag(PensumUiTags.modalityOption("long_internship")).performClick()
			onNodeWithTag(PensumUiTags.versionOption(year = 2018)).performClick()
			waitForIdle()

			onNodeWithTag(PensumUiTags.versionOption(year = 2018)).assertIsSelected()
			onNodeWithTag(PensumUiTags.versionOption(year = 2019)).assertIsNotSelected()
			onNodeWithTag(PensumUiTags.modalityOption("degree_project")).assertIsSelected()
			assertNodeHidden(PensumUiTags.modalityOption("long_internship"))
			onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
			waitUntil(timeoutMillis = TIMEOUT_MILLIS) { recorder.dismissCount == 1 }

			assertEquals(listOf(2018 to "degree_project"), recorder.appliedSelections)
		}

	@Test
	fun when_cancelIsTapped_then_dismissesWithoutApplyingTheDraftSelection() = runTuIndiceUiTest {
		val recorder = SelectionSheetRecorder()
		setSelectionSheetContent(recorder)

		onNodeWithTag(PensumUiTags.versionOption(year = 2018)).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { recorder.dismissCount == 1 }

		assertEquals(emptyList(), recorder.appliedSelections)
	}

	@Test
	fun when_modelHasNoPensumOptions_then_sheetIsNotRendered() = runTuIndiceUiTest {
		setSelectionSheetContent(
			recorder = SelectionSheetRecorder(),
			model = samplePensumScreenModelWithSelectableYears().copy(pensumOptions = emptyList())
		)

		onAllNodesWithText("Cambiar pensum").assertCountEquals(0)
		assertNodeHidden(PensumUiTags.PensumCurrentSelectionSummary)
		assertNodeHidden(BaseUiTags.ConfirmationDialogPositiveButton)
	}
}

private class SelectionSheetRecorder {
	val appliedSelections = mutableListOf<Pair<Int, String>>()
	var dismissCount = 0
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setSelectionSheetContent(
	recorder: SelectionSheetRecorder,
	model: PensumScreenModel = samplePensumScreenModelWithSelectableYears()
) {
	setTuIndiceTestContent {
		PensumSelectionBottomSheet(
			model = model,
			onSelectionApplied = { pensum, modality ->
				recorder.appliedSelections += pensum.year to modality.id
			},
			onDismissRequest = { recorder.dismissCount += 1 }
		)
	}
}

private const val TIMEOUT_MILLIS = 5_000L
