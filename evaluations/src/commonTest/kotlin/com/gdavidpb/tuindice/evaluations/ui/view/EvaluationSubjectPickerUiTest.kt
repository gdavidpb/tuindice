package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.runtime.mutableStateOf
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationAttemptPickerItems
import com.gdavidpb.tuindice.evaluations.testing.uiSubjects
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationSubjectPickerUiTest {
	@Test
	fun when_noAttemptIsSelected_then_attemptChipTapped_invokesSelectionCallback() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		var selectedAttempt: EditableAttemptDescriptor? = null

		setTuIndiceTestContent {
			EvaluationAttemptPicker(
				items = subjects.toEvaluationAttemptPickerItems(selectedAttempt = null),
				onAttemptChange = { attempt -> selectedAttempt = attempt }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSubjectPickerRow)
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[0].id))
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[1].id))

		onNodeWithTag(
			EvaluationsUiTags.evaluationSubjectChip(subjects[1].id)
		).performClick()

		assertEquals(subjects[1], selectedAttempt)
	}

	@Test
	fun when_selectedAttemptChipTapped_then_pickerClearsSelection_andShowsAllAttempts() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		val selectedAttemptState = mutableStateOf<EditableAttemptDescriptor?>(subjects.first())

		setTuIndiceTestContent {
			EvaluationAttemptPicker(
				items = subjects.toEvaluationAttemptPickerItems(
					selectedAttempt = selectedAttemptState.value
				),
				onAttemptChange = { attempt -> selectedAttemptState.value = attempt }
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[0].id))
		assertNodeHidden(EvaluationsUiTags.evaluationSubjectChip(subjects[1].id))

		onNodeWithTag(
			EvaluationsUiTags.evaluationSubjectChip(subjects[0].id)
		).performClick()

		assertEquals(null, selectedAttemptState.value)
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[0].id))
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[1].id))
	}

	@Test
	fun when_pickerIsDisabled_then_tappingChipDoesNotInvokeCallback() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		var selectedAttempt: EditableAttemptDescriptor? = null

		setTuIndiceTestContent {
			EvaluationAttemptPicker(
				enabled = false,
				items = subjects.toEvaluationAttemptPickerItems(selectedAttempt = null),
				onAttemptChange = { attempt -> selectedAttempt = attempt }
			)
		}

		onNodeWithTag(
			EvaluationsUiTags.evaluationSubjectChip(subjects[1].id)
		).performClick()

		assertEquals(null, selectedAttempt)
	}

	@Test
	fun when_selectedAttemptChangesExternally_then_pickerCollapsesToTheNewSelection() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		val selectedAttemptState = mutableStateOf<EditableAttemptDescriptor?>(null)

		setTuIndiceTestContent {
			EvaluationAttemptPicker(
				items = subjects.toEvaluationAttemptPickerItems(
					selectedAttempt = selectedAttemptState.value
				),
				onAttemptChange = {}
			)
		}

		runOnIdle {
			selectedAttemptState.value = subjects[1]
		}

		assertNodeHidden(EvaluationsUiTags.evaluationSubjectChip(subjects[0].id))
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[1].id))

		onNodeWithTag(
			EvaluationsUiTags.evaluationSubjectChip(subjects[1].id)
		).assert(
			SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)
		)
	}
}
