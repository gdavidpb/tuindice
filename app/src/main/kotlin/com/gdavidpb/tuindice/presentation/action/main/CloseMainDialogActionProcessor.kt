package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow

class CloseMainDialogActionProcessor
	: ActionProcessor<Main.State, Main.Action.CloseDialog, Main.Effect>() {

	override fun process(
		action: Main.Action.CloseDialog,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		sideEffect(
			Main.Effect.CloseDialog
		)

		return super.process(action, sideEffect)
	}
}