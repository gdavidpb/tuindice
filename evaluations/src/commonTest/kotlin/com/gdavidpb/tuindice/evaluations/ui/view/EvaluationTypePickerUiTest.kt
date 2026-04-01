package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.runtime.mutableStateOf
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationTypePickerUiTest {
	@Test
	fun when_noTypeIsSelected_then_typeChipTapped_invokesSelectionCallback() = runTuIndiceUiTest {
		var selectedType: EvaluationType? = null

		setTuIndiceTestContent {
			EvaluationTypePicker(
				selectedType = null,
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
	fun when_selectedTypeChipTapped_then_pickerClearsSelection_andShowsAllTypes() = runTuIndiceUiTest {
		val selectedTypeState = mutableStateOf<EvaluationType?>(EvaluationType.QUIZ)

		setTuIndiceTestContent {
			EvaluationTypePicker(
				selectedType = selectedTypeState.value,
				onTypeChange = { type -> selectedTypeState.value = type }
			)
		}

		onNodeWithTag(
			EvaluationsUiTags.evaluationTypeChip(EvaluationType.QUIZ.name)
		).performClick()

		assertEquals(null, selectedTypeState.value)
		assertNodeVisible(EvaluationsUiTags.evaluationTypeChip(EvaluationType.QUIZ.name))
		assertNodeVisible(EvaluationsUiTags.evaluationTypeChip(EvaluationType.TEST.name))
	}

	@Test
	fun when_selectedTypeChangesExternally_then_pickerCollapsesToThatType() = runTuIndiceUiTest {
		val selectedTypeState = mutableStateOf<EvaluationType?>(null)

		setTuIndiceTestContent {
			EvaluationTypePicker(
				selectedType = selectedTypeState.value,
				onTypeChange = {}
			)
		}

		runOnIdle {
			selectedTypeState.value = EvaluationType.PROJECT
		}

		assertNodeHidden(EvaluationsUiTags.evaluationTypeChip(EvaluationType.QUIZ.name))
		onNodeWithTag(
			EvaluationsUiTags.evaluationTypeChip(EvaluationType.PROJECT.name)
		).assert(
			SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)
		)
	}
}
