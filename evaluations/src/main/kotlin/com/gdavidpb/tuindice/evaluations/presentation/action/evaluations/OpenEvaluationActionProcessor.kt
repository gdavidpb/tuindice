package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow

class OpenEvaluationActionProcessor
	: ActionProcessor<Evaluations.State, Evaluations.Action.EditEvaluation, Evaluations.Effect>() {

	override fun process(
		action: Evaluations.Action.EditEvaluation,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		sideEffect(
			Evaluations.Effect.NavigateToEvaluation(
				evaluationId = action.evaluationId
			)
		)

		return super.process(action, sideEffect)
	}
}