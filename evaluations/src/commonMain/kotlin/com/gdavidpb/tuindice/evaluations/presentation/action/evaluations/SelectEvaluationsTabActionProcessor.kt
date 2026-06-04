package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SelectEvaluationsTabActionProcessor :
	ActionProcessor<Evaluations.State, Evaluations.Action.SelectTab, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.SelectTab,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return flowOf { state ->
			if (state is Evaluations.State.Content) {
				state.copy(
					selectedTab = action.tab,
					evaluationWeekGroups = when (action.tab) {
						EvaluationsTab.Upcoming -> state.upcomingWeekGroups
						EvaluationsTab.History -> state.historyWeekGroups
					},
					evaluationGroups = when (action.tab) {
						EvaluationsTab.Upcoming -> state.upcomingGroups
						EvaluationsTab.History -> state.historyGroups
					}
				)
			} else {
				state
			}
		}
	}
}
