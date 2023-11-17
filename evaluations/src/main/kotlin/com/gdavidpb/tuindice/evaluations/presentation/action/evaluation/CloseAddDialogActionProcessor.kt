package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow

class CloseAddDialogActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.CloseDialog, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.CloseDialog,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		sideEffect(
			Evaluation.Effect.CloseDialog
		)

		return super.process(action, sideEffect)
	}
}