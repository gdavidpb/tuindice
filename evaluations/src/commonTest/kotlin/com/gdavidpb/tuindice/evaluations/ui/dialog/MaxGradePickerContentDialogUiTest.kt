package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class MaxGradePickerContentDialogUiTest {
	@Test
	fun when_maxGradePickerContentDialogConfirmed_then_emitsGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null

		setTuIndiceTestContent {
			MaxGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 20.0,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(20.0, selectedGrade)
	}

	@Test
	fun when_maxGradePickerContentDialogHasNullGrade_then_confirmEmitsDefaultMaxGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null

		setTuIndiceTestContent {
			MaxGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = null,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(100.0, selectedGrade)
	}

	@Test
	fun when_maxGradePickerContentDialogHasZeroGrade_then_confirmEmitsDefaultMaxGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null

		setTuIndiceTestContent {
			MaxGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 0.0,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(100.0, selectedGrade)
	}

	@Test
	fun when_theDialogOpens_then_itAsksForTheMaxGrade_andNamesTheEvaluation() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			MaxGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogTitle)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogTitle).assertTextEquals("Nota máxima")
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).assertTextEquals("Aceptar")
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogDismissButton).assertTextEquals("Cancelar")
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogSubtitle)
		onNodeWithText("Parcial 1").assertExists()
		onNodeWithText("MA1111").assertExists()
	}

	@Test
	fun when_cancelIsTapped_then_theDialogAsksToClose_andNoGradeIsEmitted() = runTuIndiceUiTest {
		var selectedGrade: Double? = null
		var dismissRequests = 0

		setTuIndiceTestContent {
			MaxGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 20.0,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogDismissButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogDismissButton).performClick()

		assertEquals(null, selectedGrade)
		assertEquals(1, dismissRequests)
	}
}
