package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.withActiveFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UncheckEvaluationFilterActionProcessor
	: ActionProcessor<Evaluations.State, Evaluations.Action.UncheckEvaluationFilter, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.UncheckEvaluationFilter,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return flowOf { state ->
			if (state is Evaluations.State.Content) {
				val activeFilters = state.activeFilters - action.filter

				state.copy(
					activeFilters = activeFilters,
					filterGroups = state.filterGroups.withActiveFilters(activeFilters)
				)
			}
			else
				state
		}
	}
}
