package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.min

class SetMaxGradeActionProcessor
	:
	ActionProcessor<Evaluation.State, Evaluation.Action.SetMaxGrade, Evaluation.Effect>() {

	override fun process(
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