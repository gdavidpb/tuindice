package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetTypeActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.SetType, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.SetType,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content)
				state.copy(
					type = action.type
				)
			else
				state
		}
	}
}