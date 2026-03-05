package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.testing.evaluationContentState
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class EvaluationContentViewUiTest {
	private data class DonePayload(
		val subject: Subject?,
		val type: EvaluationType?,
		val date: Long?,
		val grade: Double?,
		val maxGrade: Double?
	)

	@Test
	fun when_doneTapped_then_invokesDoneCallbackWithCurrentForm() = runTuIndiceUiTest {
		val state = evaluationContentState(isOverdue = false)
		var maxGradeClicks = 0
		var donePayload: DonePayload? = null

		setTuIndiceTestContent {
			EvaluationContentView(
				state = state,
				onSubjectChange = {},
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { _, _ -> },
				onMaxGradeClick = { _ -> maxGradeClicks++ },
				onDoneClick = { subject, type, date, grade, maxGrade ->
					donePayload = DonePayload(subject, type, date, grade, maxGrade)
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationDatePicker)
		assertNodeHidden(EvaluationsUiTags.EvaluationGradeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationMaxGradeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationDoneFab)

		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeChip).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationDoneFab).performClick()

		val payload = donePayload!!

		assertEquals(1, maxGradeClicks)
		assertNotNull(payload)
		assertEquals(state.selectedSubject, payload.subject)
		assertEquals(state.type, payload.type)
		assertEquals(state.date, payload.date)
		assertEquals(state.grade, payload.grade)
		assertEquals(state.maxGrade, payload.maxGrade)
	}

	@Test
	fun when_stateIsOverdue_then_gradeAndMaxGradeChipsDispatchCurrentValues() = runTuIndiceUiTest {
		val state = evaluationContentState(isOverdue = true)
		var gradePayload: Pair<Double?, Double?>? = null
		var maxGradePayload: Double? = null

		setTuIndiceTestContent {
			EvaluationContentView(
				state = state,
				onSubjectChange = {},
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { grade, maxGrade ->
					gradePayload = grade to maxGrade
				},
				onMaxGradeClick = { maxGrade ->
					maxGradePayload = maxGrade
				},
				onDoneClick = { _, _, _, _, _ -> }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationGradeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationMaxGradeChip)

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeChip).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeChip).performClick()

		assertEquals(state.grade to state.maxGrade, gradePayload)
		assertEquals(state.maxGrade, maxGradePayload)
	}
}
