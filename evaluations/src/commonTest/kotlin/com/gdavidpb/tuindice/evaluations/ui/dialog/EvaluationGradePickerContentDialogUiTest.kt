package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationGradePickerContentDialogUiTest {
	@Test
	fun when_evaluationGradePickerContentDialogRendered_then_showsDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 16.0,
				maxGrade = 20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogTitle)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogSubtitle)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogSubjectCodeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogDismissButton)
	}

	@Test
	fun when_theDialogOpens_then_itOffersToEditTheGradeInTheAppsOwnWords() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 16.0,
				maxGrade = 20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogTitle)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogTitle).assertTextEquals("Modificar nota")
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).assertTextEquals("Aceptar")
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogDismissButton).assertTextEquals("Cancelar")
	}

	@Test
	fun when_confirmIsTapped_then_emitsTheSelectedGrade_andAsksToClose() = runTuIndiceUiTest {
		var selectedGrade: Double? = null
		var dismissRequests = 0

		setTuIndiceTestContent {
			EvaluationGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 16.0,
				maxGrade = 20.0,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(16.0, selectedGrade)
		assertEquals(1, dismissRequests)
	}

	@Test
	fun when_theEvaluationHasNoGradeYet_then_confirmEmitsTheMinimumGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = null,
				maxGrade = 20.0,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(MIN_EVALUATION_GRADE, selectedGrade)
	}

	@Test
	fun when_dismissOnConfirmIsFalse_then_confirmEmitsTheGrade_andTheDialogIsLeftToTheCaller() = runTuIndiceUiTest {
		var selectedGrade: Double? = null
		var dismissRequests = 0

		setTuIndiceTestContent {
			EvaluationGradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 16.0,
				maxGrade = 20.0,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = { dismissRequests++ },
				dismissOnConfirm = false
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(16.0, selectedGrade)
		assertEquals(0, dismissRequests)
	}
}
