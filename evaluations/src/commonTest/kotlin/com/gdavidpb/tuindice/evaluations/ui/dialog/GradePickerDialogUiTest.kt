package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class GradePickerDialogUiTest {
	@Test
	fun when_confirmTapped_then_emitsGradeAndDismisses() = runTuIndiceUiTest {
		var changedGrade: Double? = null
		var dismissCalls = 0

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = 18.75,
				gradeRange = 0.0..20.0,
				onGradeChange = { grade ->
					changedGrade = grade
				},
				onDismissRequest = {
					dismissCalls++
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogTitle)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogDismissButton)

		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(18.75, changedGrade)
		assertEquals(1, dismissCalls)
	}

	@Test
	fun when_subtitleProvided_then_showsSubtitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Modificar nota",
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = 18.75,
				gradeRange = 0.0..20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogSubtitle)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogSubjectCodeChip)
		onNodeWithText("Parcial 1").assertExists()
		onNodeWithText("MA1111").assertExists()
	}

	@Test
	fun when_subtitleValuesAreBlank_then_hidesSubtitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "",
				subjectCode = "",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = null,
				gradeRange = 0.0..20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogTitle)
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogSubtitle).assertDoesNotExist()
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogSubjectCodeChip).assertDoesNotExist()
	}

	@Test
	fun when_onlySubjectCodeProvided_then_showsChipWithoutBullet() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = null,
				gradeRange = 0.0..20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogSubtitle)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogSubjectCodeChip)
		onNodeWithText("MA1111").assertExists()
		onNodeWithText("•").assertDoesNotExist()
	}

	@Test
	fun when_cancelTapped_then_dismissesWithoutChangingGrade() = runTuIndiceUiTest {
		var changedGrade: Double? = null
		var dismissCalls = 0

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = 18.75,
				gradeRange = 0.0..20.0,
				onGradeChange = { grade ->
					changedGrade = grade
				},
				onDismissRequest = {
					dismissCalls++
				}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDialogDismissButton).performClick()

		assertEquals(null, changedGrade)
		assertEquals(1, dismissCalls)
	}

	@Test
	fun when_selectedGradeIsNullAndConfirmTapped_then_emitsMinimumGrade() = runTuIndiceUiTest {
		var changedGrade: Double? = null
		var dismissCalls = 0

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = null,
				gradeRange = 0.0..20.0,
				onGradeChange = { grade ->
					changedGrade = grade
				},
				onDismissRequest = {
					dismissCalls++
				}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(MIN_EVALUATION_GRADE, changedGrade)
		assertEquals(1, dismissCalls)
	}

	@Test
	fun when_selectedGradeChangesBeforeConfirm_then_emitsUpdatedGrade() = runTuIndiceUiTest {
		val selectedGradeState = mutableStateOf<Double?>(10.0)
		var changedGrade: Double? = null

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = selectedGradeState.value,
				gradeRange = 0.0..20.0,
				onGradeChange = { grade ->
					changedGrade = grade
				},
				onDismissRequest = {}
			)
		}

		runOnIdle {
			selectedGradeState.value = 17.25
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(17.25, changedGrade)
	}

	@Test
	fun when_wheelScrolledAndConfirmTappedImmediately_then_emitsScrolledGrade() = runTuIndiceUiTest {
		var changedGrade: Double? = null

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = 10.0,
				gradeRange = 0.0..20.0,
				onGradeChange = { grade ->
					changedGrade = grade
				},
				onDismissRequest = {}
			)
		}

		onAllNodesWithTag(BaseUiTags.WheelPickerList)[0]
			.performTouchInput { swipeUp() }
		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertNotNull(changedGrade)
		assertNotEquals(10.0, changedGrade)
	}

	@Test
	fun when_dismissOnConfirmIsFalse_then_confirmEmitsGradeWithoutDismissing() = runTuIndiceUiTest {
		var changedGrade: Double? = null
		var dismissCalls = 0

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
				evaluationName = "Parcial 1",
				subjectCode = "MA1111",
				acceptText = "Aceptar",
				cancelText = "Cancelar",
				selectedGrade = 18.75,
				gradeRange = 0.0..20.0,
				onGradeChange = { grade ->
					changedGrade = grade
				},
				onDismissRequest = {
					dismissCalls++
				},
				dismissOnConfirm = false
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDialogConfirmButton).performClick()

		assertEquals(18.75, changedGrade)
		assertEquals(0, dismissCalls)
	}
}
