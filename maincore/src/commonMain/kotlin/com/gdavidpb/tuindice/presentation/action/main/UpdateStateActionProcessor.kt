package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateStateActionProcessor
	: ActionProcessor<Main.State, Main.Action.UpdateState, Main.Effect>() {

	override fun process(
		action: Main.Action.UpdateState,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return flowOf { _ ->
			action.state
		}
	}
}