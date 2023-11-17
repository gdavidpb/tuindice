package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.utils.extension.isDatePassed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetDateActionProcessor
	: ActionProcessor<AddEvaluation.State, AddEvaluation.Action.SetDate, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.SetDate,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		return flowOf { state ->
			if (state is AddEvaluation.State.Content)
				state.copy(
					date = action.date,
					isOverdue = action.date.isDatePassed()
				)
			else
				state
		}
	}
}