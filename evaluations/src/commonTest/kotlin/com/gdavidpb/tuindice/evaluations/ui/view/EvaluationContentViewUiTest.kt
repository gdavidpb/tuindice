package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationRequiredField
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.evaluationContentState
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class EvaluationContentViewUiTest {
	@Test
	fun when_doneTapped_then_invokesDoneCallback() = runTuIndiceUiTest {
		val state = evaluationContentState(isOverdue = false)
		var maxGradeClicks = 0
		var doneDispatches = 0

		setTuIndiceTestContent {
			EvaluationContentView(
					state = state,
					onAttemptChange = {},
					onTypeChange = {},
					onDateChange = {},
					onGradeClick = { _, _, _, _ -> },
					onMaxGradeClick = { _, _, _ -> maxGradeClicks++ },
				onDoneClick = { doneDispatches++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationDatePicker)
		assertNodeHidden(EvaluationsUiTags.EvaluationGradeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationMaxGradeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationDoneFab)
		onNodeWithText("Crear").assertExists()

		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeChip).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationDoneFab).performClick()

		assertEquals(1, maxGradeClicks)
		assertEquals(1, doneDispatches)
	}

	@Test
	fun when_missingFieldsFlagged_then_showsInlineRequiredErrors() = runTuIndiceUiTest {
		val state = evaluationContentState(isOverdue = false).copy(
			missingFields = setOf(
				EvaluationRequiredField.SUBJECT,
				EvaluationRequiredField.TYPE,
				EvaluationRequiredField.MAX_GRADE
			)
		)

		setTuIndiceTestContent {
			EvaluationContentView(
					state = state,
					onAttemptChange = {},
					onTypeChange = {},
					onDateChange = {},
					onGradeClick = { _, _, _, _ -> },
					onMaxGradeClick = { _, _, _ -> },
				onDoneClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSubjectRequiredError)
		assertNodeVisible(EvaluationsUiTags.EvaluationTypeRequiredError)
		assertNodeVisible(EvaluationsUiTags.EvaluationMaxGradeRequiredError)
	}

	@Test
	fun when_noMissingFieldsFlagged_then_hidesInlineRequiredErrors() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationContentView(
					state = evaluationContentState(isOverdue = false),
					onAttemptChange = {},
					onTypeChange = {},
					onDateChange = {},
					onGradeClick = { _, _, _, _ -> },
					onMaxGradeClick = { _, _, _ -> },
				onDoneClick = {}
			)
		}

		assertNodeHidden(EvaluationsUiTags.EvaluationSubjectRequiredError)
		assertNodeHidden(EvaluationsUiTags.EvaluationTypeRequiredError)
		assertNodeHidden(EvaluationsUiTags.EvaluationMaxGradeRequiredError)
	}

	@Test
	fun when_stateIsEditMode_then_doneButtonShowsModifyCopy() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationContentView(
					state = evaluationContentState(isOverdue = false).copy(evaluationId = "evaluation_1"),
					onAttemptChange = {},
					onTypeChange = {},
					onDateChange = {},
					onGradeClick = { _, _, _, _ -> },
					onMaxGradeClick = { _, _, _ -> },
				onDoneClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDoneFab)
		onNodeWithText("Modificar").assertExists()
	}

	@Test
	fun when_stateIsSaving_then_doneButtonShowsProgressAndIsDisabled() = runTuIndiceUiTest {
		var doneClicks = 0

		setTuIndiceTestContent {
			EvaluationContentView(
					state = evaluationContentState(isOverdue = false).copy(isSubmitting = true),
					onAttemptChange = {},
					onTypeChange = {},
					onDateChange = {},
					onGradeClick = { _, _, _, _ -> },
					onMaxGradeClick = { _, _, _ -> },
				onDoneClick = { doneClicks++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDoneProgress)
		onNodeWithTag(EvaluationsUiTags.EvaluationDoneFab)
			.assertIsNotEnabled()

		assertEquals(0, doneClicks)
	}

	@Test
		fun when_stateIsOverdue_then_gradeAndMaxGradeChipsDispatchCurrentValues() = runTuIndiceUiTest {
			val state = evaluationContentState(isOverdue = true)
			var gradeEvaluationName = ""
			var gradeSubjectCode = ""
			var gradePayload: Pair<Double?, Double?>? = null
			var maxGradeEvaluationName = ""
			var maxGradeSubjectCode = ""
			var maxGradePayload: Double? = null

		setTuIndiceTestContent {
			EvaluationContentView(
				state = state,
					onAttemptChange = {},
					onTypeChange = {},
					onDateChange = {},
					onGradeClick = { evaluationName, subjectCode, grade, maxGrade ->
						gradeEvaluationName = evaluationName
						gradeSubjectCode = subjectCode
						gradePayload = grade to maxGrade
					},
					onMaxGradeClick = { evaluationName, subjectCode, maxGrade ->
						maxGradeEvaluationName = evaluationName
						maxGradeSubjectCode = subjectCode
						maxGradePayload = maxGrade
					},
				onDoneClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationGradeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationMaxGradeChip)

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeChip).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeChip).performClick()

			assertEquals("Quiz 1", gradeEvaluationName)
			assertEquals(DEFAULT_EVALUATION_SUBJECT.code, gradeSubjectCode)
			assertEquals(state.grade to state.maxGrade, gradePayload)
			assertEquals("Quiz 1", maxGradeEvaluationName)
			assertEquals(DEFAULT_EVALUATION_SUBJECT.code, maxGradeSubjectCode)
			assertEquals(state.maxGrade, maxGradePayload)
	}

	@Test
	fun when_stateIsOverdueWithoutSelectedMaxGrade_then_onlyMaxGradeChipIsVisible() = runTuIndiceUiTest {
		val state = evaluationContentState(
			isOverdue = true,
			grade = null,
			maxGrade = 0.0
		)
		var gradeClicks = 0
		var maxGradePayload: Double? = null

		setTuIndiceTestContent {
			EvaluationContentView(
					state = state,
					onAttemptChange = {},
					onTypeChange = {},
					onDateChange = {},
					onGradeClick = { _, _, _, _ -> gradeClicks++ },
					onMaxGradeClick = { _, _, maxGrade ->
						maxGradePayload = maxGrade
					},
				onDoneClick = {}
			)
		}

		assertNodeHidden(EvaluationsUiTags.EvaluationGradeChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationMaxGradeChip)

		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeChip).performClick()

		assertEquals(0, gradeClicks)
		assertEquals(state.maxGrade, maxGradePayload)
	}

	@Test
	fun when_theEvaluationIsNew_then_theSubjectIsPickedFromTheChips() = runTuIndiceUiTest {
		val state = evaluationContentState(isOverdue = false).copy(evaluationId = null)
		var attemptChanges = 0

		setTuIndiceTestContent {
			EvaluationContentView(
				state = state,
				onAttemptChange = { attemptChanges++ },
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { _, _, _, _ -> },
				onMaxGradeClick = { _, _, _ -> },
				onDoneClick = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationSubjectChip(DEFAULT_EVALUATION_SUBJECT.id)).performClick()

		// The only chip showing is the selected one: tapping it clears the choice.
		assertEquals(1, attemptChanges)
	}

	@Test
	fun when_theEvaluationIsBeingEdited_then_theSubjectChipCannotBeChanged() = runTuIndiceUiTest {
		val state = evaluationContentState(isOverdue = false).copy(evaluationId = "evaluation-1")
		var attemptChanges = 0

		setTuIndiceTestContent {
			EvaluationContentView(
				state = state,
				onAttemptChange = { attemptChanges++ },
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { _, _, _, _ -> },
				onMaxGradeClick = { _, _, _ -> },
				onDoneClick = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationSubjectChip(DEFAULT_EVALUATION_SUBJECT.id))
			.assertIsNotEnabled()
			.performClick()

		assertEquals(0, attemptChanges)
	}

	@Test
	fun when_theChosenSubjectHasClass_then_theDateDialogMarksItsClassDaysAndSaysWhereOnTheChosenOne() = runTuIndiceUiTest {
		// 2026-10-12 is a Monday; the subject meets on Mondays.
		val monday = LocalDate(2026, 10, 12)
		val subject = DEFAULT_EVALUATION_SUBJECT.copy(
			meetings = listOf(
				ScheduleMeeting(
					attemptId = DEFAULT_EVALUATION_SUBJECT.id,
					subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
					section = 1,
					dayOfWeek = 2,
					startBlock = 1,
					endBlock = 2,
					classroom = "MYS-116"
				)
			)
		)
		val state = evaluationContentState(isOverdue = false).copy(
			selectedAttempt = subject,
			date = monday.toEvaluationEpochMillis()
		)

		setTuIndiceTestContent {
			EvaluationContentView(
				state = state,
				onAttemptChange = {},
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { _, _, _, _ -> },
				onMaxGradeClick = { _, _, _ -> },
				onDoneClick = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()

		// The subject of the dialog is the one chosen in the form, with its meetings.
		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogSubjectChip)
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText("En clase · 1-2 · MYS-116").fetchSemanticsNodes().isNotEmpty()
		}
		assertNodeVisible(EvaluationsUiTags.calendarDayClassDot(12))
		assertNodeVisible(EvaluationsUiTags.calendarDayClassDot(19))
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
	}
}
