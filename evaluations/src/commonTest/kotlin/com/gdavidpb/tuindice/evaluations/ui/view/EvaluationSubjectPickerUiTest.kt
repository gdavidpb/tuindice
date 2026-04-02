package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.runtime.mutableStateOf
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationSubjectPickerItemList
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
	fun when_noSubjectIsSelected_then_subjectChipTapped_invokesSelectionCallback() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		var selectedSubject: Subject? = null

		setTuIndiceTestContent {
			EvaluationSubjectPicker(
				items = subjects.toEvaluationSubjectPickerItemList(selectedSubject = null),
				onSubjectChange = { subject -> selectedSubject = subject }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSubjectPickerRow)
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[0].id))
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[1].id))

		onNodeWithTag(
			EvaluationsUiTags.evaluationSubjectChip(subjects[1].id)
		).performClick()

		assertEquals(subjects[1], selectedSubject)
	}

	@Test
	fun when_selectedSubjectChipTapped_then_pickerClearsSelection_andShowsAllSubjects() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		val selectedSubjectState = mutableStateOf<Subject?>(subjects.first())

		setTuIndiceTestContent {
			EvaluationSubjectPicker(
				items = subjects.toEvaluationSubjectPickerItemList(
					selectedSubject = selectedSubjectState.value
				),
				onSubjectChange = { subject -> selectedSubjectState.value = subject }
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[0].id))
		assertNodeHidden(EvaluationsUiTags.evaluationSubjectChip(subjects[1].id))

		onNodeWithTag(
			EvaluationsUiTags.evaluationSubjectChip(subjects[0].id)
		).performClick()

		assertEquals(null, selectedSubjectState.value)
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[0].id))
		assertNodeVisible(EvaluationsUiTags.evaluationSubjectChip(subjects[1].id))
	}

	@Test
	fun when_pickerIsDisabled_then_tappingChipDoesNotInvokeCallback() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		var selectedSubject: Subject? = null

		setTuIndiceTestContent {
			EvaluationSubjectPicker(
				enabled = false,
				items = subjects.toEvaluationSubjectPickerItemList(selectedSubject = null),
				onSubjectChange = { subject -> selectedSubject = subject }
			)
		}

		onNodeWithTag(
			EvaluationsUiTags.evaluationSubjectChip(subjects[1].id)
		).performClick()

		assertEquals(null, selectedSubject)
	}

	@Test
	fun when_selectedSubjectChangesExternally_then_pickerCollapsesToTheNewSelection() = runTuIndiceUiTest {
		val subjects = uiSubjects()
		val selectedSubjectState = mutableStateOf<Subject?>(null)

		setTuIndiceTestContent {
			EvaluationSubjectPicker(
				items = subjects.toEvaluationSubjectPickerItemList(
					selectedSubject = selectedSubjectState.value
				),
				onSubjectChange = {}
			)
		}

		runOnIdle {
			selectedSubjectState.value = subjects[1]
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
