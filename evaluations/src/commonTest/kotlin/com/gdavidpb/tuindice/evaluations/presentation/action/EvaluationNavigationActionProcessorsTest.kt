package com.gdavidpb.tuindice.evaluations.presentation.action

import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EvaluationNavigationActionProcessorsTest {
	@Test
	fun openAddEvaluationActionProcessor_emitsNavigateToAddEvaluation() = runBlocking {
		val processor = OpenAddEvaluationActionProcessor()
		val effects = mutableListOf<Evaluations.Effect>()

		processor.process(
			action = Evaluations.Action.AddEvaluation,
			sideEffect = effects::add
		).toList()

		assertEquals(Evaluations.Effect.NavigateToAddEvaluation, effects.single())
	}

	@Test
	fun openEvaluationActionProcessor_emitsNavigateToEvaluation() = runBlocking {
		val processor = OpenEvaluationActionProcessor()
		val effects = mutableListOf<Evaluations.Effect>()
		val evaluationId = "evaluation-123"

		processor.process(
			action = Evaluations.Action.EditEvaluation(evaluationId),
			sideEffect = effects::add
		).toList()

		val effect = assertIs<Evaluations.Effect.NavigateToEvaluation>(effects.single())
		assertEquals(evaluationId, effect.evaluationId)
	}

	@Test
	fun pickGradeActionProcessor_emitsNavigateToGradePickerDialog() = runBlocking {
		val processor = PickGradeActionProcessor()
		val effects = mutableListOf<Evaluation.Effect>()

		processor.process(
			action = Evaluation.Action.ClickGrade(
				grade = 4.4,
				maxGrade = 5.0
			),
			sideEffect = effects::add
		).toList()

		val effect = assertIs<Evaluation.Effect.NavigateToGradePickerDialog>(effects.single())
		assertEquals(4.4, effect.grade)
		assertEquals(5.0, effect.maxGrade)
	}

	@Test
	fun pickMaxGradeActionProcessor_emitsNavigateToMaxGradePickerDialog() = runBlocking {
		val processor = PickMaxGradeActionProcessor()
		val effects = mutableListOf<Evaluation.Effect>()

		processor.process(
			action = Evaluation.Action.ClickMaxGrade(maxGrade = 5.0),
			sideEffect = effects::add
		).toList()

		val effect = assertIs<Evaluation.Effect.NavigateToMaxGradePickerDialog>(effects.single())
		assertEquals(5.0, effect.maxGrade)
	}
}
