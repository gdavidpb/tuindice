package com.gdavidpb.tuindice.evaluations.presentation.action.list

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow

class CloseListDialogActionProcessor
	: ActionProcessor<Evaluations.State, Evaluations.Action.CloseDialog, Evaluations.Effect>() {

	override fun process(
		action: Evaluations.Action.CloseDialog,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		sideEffect(
			Evaluations.Effect.CloseDialog
		)

		return super.process(action, sideEffect)
	}
}