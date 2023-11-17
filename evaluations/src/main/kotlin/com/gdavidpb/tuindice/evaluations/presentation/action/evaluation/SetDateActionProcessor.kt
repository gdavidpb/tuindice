package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.utils.extension.isDatePassed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetDateActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.SetDate, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.SetDate,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content)
				state.copy(
					date = action.date,
					isOverdue = action.date.isDatePassed()
				)
			else
				state
		}
	}
}