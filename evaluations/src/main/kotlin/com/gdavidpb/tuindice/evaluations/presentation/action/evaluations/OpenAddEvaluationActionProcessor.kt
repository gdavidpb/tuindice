package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow

class OpenAddEvaluationActionProcessor
	: ActionProcessor<Evaluations.State, Evaluations.Action.AddEvaluation, Evaluations.Effect>() {

	override fun process(
		action: Evaluations.Action.AddEvaluation,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		sideEffect(
			Evaluations.Effect.NavigateToAddEvaluation
		)

		return super.process(action, sideEffect)
	}
}