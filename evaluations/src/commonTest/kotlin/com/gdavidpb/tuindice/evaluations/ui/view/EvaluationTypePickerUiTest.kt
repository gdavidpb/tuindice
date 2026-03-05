package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationTypePickerUiTest {
	@Test
	fun when_typeChipTapped_then_invokesSelectionCallback() = runTuIndiceUiTest {
		var selectedType: EvaluationType? = null

		setTuIndiceTestContent {
			EvaluationTypePicker(
				selectedType = EvaluationType.QUIZ,
				onTypeChange = { type -> selectedType = type }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationTypePickerRow)

		onNodeWithTag(
			EvaluationsUiTags.evaluationTypeChip(EvaluationType.TEST.name)
		).performClick()

		assertEquals(EvaluationType.TEST, selectedType)
	}

	@Test
	fun when_multipleTypeChipsTapped_then_emitsLastSelectedType() = runTuIndiceUiTest {
		var selectedType: EvaluationType? = null

		setTuIndiceTestContent {
			EvaluationTypePicker(
				selectedType = EvaluationType.QUIZ,
				onTypeChange = { type -> selectedType = type }
			)
		}

		onNodeWithTag(
			EvaluationsUiTags.evaluationTypeChip(EvaluationType.ESSAY.name)
		).performClick()
		onNodeWithTag(
			EvaluationsUiTags.evaluationTypeChip(EvaluationType.PROJECT.name)
		).performClick()

		assertEquals(EvaluationType.PROJECT, selectedType)
	}
}
