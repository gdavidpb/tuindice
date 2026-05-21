package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationContentDialogUiTest {
	@Test
	fun when_gradePickerContentDialogConfirmed_then_emitsSelectedGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null

		setTuIndiceTestContent {
			GradePickerContentDialog(
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
	fun when_maxGradePickerContentDialogConfirmed_then_emitsGrade() = runTuIndiceUiTest {
		var selectedGrade: Double? = null

		setTuIndiceTestContent {
			MaxGradePickerContentDialog(
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
	fun when_evaluationGradePickerContentDialogRendered_then_showsDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationGradePickerContentDialog(
				evaluationName = "Quiz #1",
				selectedGrade = 16.0,
				maxGrade = 20.0,
				onGradeChange = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDialogTitle)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogConfirmButton)
		assertNodeVisible(EvaluationsUiTags.EvaluationDialogDismissButton)
	}
}
