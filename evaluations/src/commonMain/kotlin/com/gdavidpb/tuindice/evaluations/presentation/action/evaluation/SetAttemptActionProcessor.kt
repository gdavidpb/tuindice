package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.withSelectedAttempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetAttemptActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.SetAttempt, Evaluation.Effect> {

	override suspend fun process(
		action: Evaluation.Action.SetAttempt,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content)
				state.copy(
					selectedAttempt = action.attempt,
					attemptItems = state.attemptItems.withSelectedAttempt(action.attempt)
				)
			else
				state
		}
	}
}
