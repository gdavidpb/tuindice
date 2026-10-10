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
class GradePickerContentDialogUiTest {
	@Test
	fun when_gradePickerContentDialogConfirmed_then_emitsSelectedGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null

		setTuIndiceTestContent {
			GradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 15.5,
				maxGrade = 20.0,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(15.5, selectedGrade)
	}

	@Test
	fun when_theDialogOpens_then_itAsksForTheObtainedGradeInTheAppsOwnWords() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			GradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 15.5,
				maxGrade = 20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogTitle)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogTitle).assertTextEquals("Nota obtenida")
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).assertTextEquals("Aceptar")
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogDismissButton).assertTextEquals("Cancelar")
		assertNodeVisible(EvaluationsUiTags.EvaluationGradeWheelPicker)
	}

	@Test
	fun when_noGradeWasAssignedYet_then_confirmEmitsTheMinimumGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null
		var dismissRequests = 0

		setTuIndiceTestContent {
			GradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = null,
				maxGrade = null,
				onGradeChange = { grade ->
					selectedGrade = grade
				},
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(MIN_EVALUATION_GRADE, selectedGrade)
		assertEquals(1, dismissRequests)
	}

	@Test
	fun when_cancelIsTapped_then_theDialogAsksToClose_andNoGradeIsEmitted() = runTuIndiceUiTest {
		var selectedGrade: Double? = null
		var dismissRequests = 0

		setTuIndiceTestContent {
			GradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 15.5,
				maxGrade = 20.0,
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

	@Test
	fun when_dismissOnConfirmIsFalse_then_confirmEmitsTheGrade_andTheDialogIsLeftToTheCaller() = runTuIndiceUiTest {
		var selectedGrade: Double? = null
		var dismissRequests = 0

		setTuIndiceTestContent {
			GradePickerContentDialog(
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				selectedGrade = 12.25,
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

		assertEquals(12.25, selectedGrade)
		assertEquals(0, dismissRequests)
	}
}
