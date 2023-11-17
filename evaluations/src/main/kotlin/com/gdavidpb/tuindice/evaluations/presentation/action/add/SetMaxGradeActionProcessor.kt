package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.min

class SetMaxGradeActionProcessor
	:
	ActionProcessor<AddEvaluation.State, AddEvaluation.Action.SetMaxGrade, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.SetMaxGrade,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		return flowOf { state ->
			if (state is AddEvaluation.State.Content)
				state.copy(
					grade = min(state.grade ?: MIN_EVALUATION_GRADE, action.maxGrade),
					maxGrade = action.maxGrade
				)
			else
				state
		}
	}
}