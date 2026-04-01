package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class GradePickerDialogUiTest {
	@Test
	fun when_confirmTapped_then_emitsGradeAndDismisses() = runTuIndiceUiTest {
		var changedGrade: Double? = null
		var dismissCalls = 0

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
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
	fun when_cancelTapped_then_dismissesWithoutChangingGrade() = runTuIndiceUiTest {
		var changedGrade: Double? = null
		var dismissCalls = 0

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
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
	fun when_dismissOnConfirmIsFalse_then_confirmEmitsGradeWithoutDismissing() = runTuIndiceUiTest {
		var changedGrade: Double? = null
		var dismissCalls = 0

		setTuIndiceTestContent {
			GradePickerDialog(
				title = "Nota maxima",
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
