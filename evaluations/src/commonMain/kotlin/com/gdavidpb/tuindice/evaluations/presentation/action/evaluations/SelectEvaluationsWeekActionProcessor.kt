package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SelectEvaluationsWeekActionProcessor :
	ActionProcessor<Evaluations.State, Evaluations.Action.SelectWeek, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.SelectWeek,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return flowOf { state ->
			if (state is Evaluations.State.Content) {
				val selectedWeekNumber = action.weekNumber.coerceIn(MIN_WEEK_NUMBER, MAX_WEEK_NUMBER)

				state.copy(
					selectedWeekNumber = selectedWeekNumber,
					weekItem = state.weekItems.firstOrNull { item ->
						item.weekNumber == selectedWeekNumber
					} ?: state.weekItem
				)
			} else {
				state
			}
		}
	}
}

private const val MIN_WEEK_NUMBER = 1
private const val MAX_WEEK_NUMBER = 12
