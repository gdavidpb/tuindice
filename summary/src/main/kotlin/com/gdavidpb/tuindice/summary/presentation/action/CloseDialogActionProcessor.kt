package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow

class CloseDialogActionProcessor
	: ActionProcessor<Summary.State, Summary.Action.CloseDialog, Summary.Effect>() {

	override fun process(
		action: Summary.Action.CloseDialog,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		sideEffect(
			Summary.Effect.CloseDialog
		)

		return super.process(action, sideEffect)
	}
}