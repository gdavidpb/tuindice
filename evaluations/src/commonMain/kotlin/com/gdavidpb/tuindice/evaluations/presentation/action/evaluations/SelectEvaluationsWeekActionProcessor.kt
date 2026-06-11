package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SelectEvaluationsWeekActionProcessor :
	ActionProcessor<Evaluations.State, Evaluations.Action.SelectWeek, Evaluations.Effect> {

	override suspend fun process(
		action: Evaluations.Action.SelectWeek,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return flowOf { state ->
			if (state is Evaluations.State.Content) {
				state.weekItems
					.firstOrNull { item -> item.key == action.weekKey }
					?.let { selectedWeekItem ->
						state.copy(
							selectedWeekKey = selectedWeekItem.key,
							weekItem = selectedWeekItem
						)
					} ?: state
			} else {
				state
			}
		}
	}
}
