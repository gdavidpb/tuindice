package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.min

private const val MIN_EVALUATION_GRADE = 0.0

class SetMaxGradeActionProcessor
	:
	ActionProcessor<Evaluation.State, Evaluation.Action.SetMaxGrade, Evaluation.Effect>() {

	override suspend fun process(
		action: Evaluation.Action.SetMaxGrade,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content)
				state.copy(
					grade = min(state.grade ?: MIN_EVALUATION_GRADE, action.maxGrade),
					maxGrade = action.maxGrade
				)
			else
				state
		}
	}
}
