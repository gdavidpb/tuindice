package com.gdavidpb.tuindice.evaluations.presentation.action.list

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CheckEvaluationFilterActionProcessor
	: ActionProcessor<Evaluations.State, Evaluations.Action.CheckEvaluationFilter, Evaluations.Effect>() {

	override fun process(
		action: Evaluations.Action.CheckEvaluationFilter,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return flowOf { state ->
			if (state is Evaluations.State.Content)
				state.copy(
					activeFilters = state.activeFilters + action.filter
				)
			else
				state
		}
	}
}