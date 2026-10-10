package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermSubmitBarViewUiTest {
	@Test
	fun when_aNewTermCanBeSubmitted_then_theBarCountsTheSubjects_andCreateReportsItsTap() = runTuIndiceUiTest {
		var createClicks = 0

		setTuIndiceTestContent {
			SubmitBar(selectedCount = 2, canSubmit = true, onCreateClick = { createClicks++ })
		}

		onNodeWithText("2 materias seleccionadas").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton)
			.assertTextEquals("Crear")
			.assertIsEnabled()
			.performClick()

		assertEquals(1, createClicks)
		assertNodeHidden(RecordUiTags.CreateSyntheticTermSubmitError)
		assertNodeHidden(RecordUiTags.CreateSyntheticTermSubmitProgress, useUnmergedTree = true)
	}

	@Test
	fun when_oneSubjectIsSelected_then_theCountIsSaidInSingular() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubmitBar(selectedCount = 1, canSubmit = true)
		}

		onNodeWithText("1 materia seleccionada").assertIsDisplayed()
		onAllNodesWithText("materias", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_nothingCanBeSubmitted_then_theButtonIsDisabled_andTappingItReportsNothing() = runTuIndiceUiTest {
		var createClicks = 0

		setTuIndiceTestContent {
			SubmitBar(selectedCount = 0, canSubmit = false, onCreateClick = { createClicks++ })
		}

		onNodeWithText("0 materias seleccionadas").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton)
			.assertIsNotEnabled()
			.performClick()

		assertEquals(0, createClicks)
	}

	@Test
	fun when_anExistingTermIsBeingEdited_then_theButtonOffersToModifyIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubmitBar(selectedCount = 3, canSubmit = true, isEditing = true)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton).assertTextEquals("Modificar")
		onAllNodesWithText("Crear").assertCountEquals(0)
	}

	@Test
	fun when_theTermIsBeingSubmitted_then_aSpinnerTakesTheLabelsPlace() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubmitBar(selectedCount = 2, canSubmit = false, isSubmitting = true)
		}

		assertNodeVisible(RecordUiTags.CreateSyntheticTermSubmitProgress, useUnmergedTree = true)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton).assertIsNotEnabled()
		onAllNodesWithText("Crear").assertCountEquals(0)
		onAllNodesWithText("Modificar").assertCountEquals(0)
	}

	@Test
	fun when_theSubmissionFailed_then_theErrorIsShownAboveTheCountAndTheButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubmitBar(
				selectedCount = 2,
				canSubmit = true,
				isEditing = false,
				isSubmitting = false,
				submitError = UiText.Raw("Ya existe un trimestre con ese periodo."),
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitError)
			.assertIsDisplayed()
			.assertTextEquals("Ya existe un trimestre con ese periodo.")

		val error = onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitError).getUnclippedBoundsInRoot()
		val button = onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton).getUnclippedBoundsInRoot()

		assertTrue(error.bottom <= button.top, "the error is read before the button it explains")
		// The student can still fix the selection and try again.
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton).assertIsEnabled()
	}

	// A bar with nothing to report: the error has a test of its own.
	@Composable
	private fun SubmitBar(
		selectedCount: Int,
		canSubmit: Boolean,
		isEditing: Boolean = false,
		isSubmitting: Boolean = false,
		onCreateClick: () -> Unit = {}
	) {
		CreateTermSubmitBar(
			selectedCount = selectedCount,
			canSubmit = canSubmit,
			isEditing = isEditing,
			isSubmitting = isSubmitting,
			submitError = UiText.Empty,
			onCreateClick = onCreateClick
		)
	}
}
