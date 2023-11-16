package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow

class CloseAddDialogActionProcessor
	: ActionProcessor<AddEvaluation.State, AddEvaluation.Action.CloseDialog, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.CloseDialog,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		sideEffect(
			AddEvaluation.Effect.CloseDialog
		)

		return super.process(action, sideEffect)
	}
}