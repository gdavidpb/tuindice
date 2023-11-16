package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetLastScreenActionProcessor(
	private val setLastScreenUseCase: SetLastScreenUseCase
) : ActionProcessor<Main.State, Main.Action.SetLastScreen, Main.Effect>() {

	override fun process(
		action: Main.Action.SetLastScreen,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return setLastScreenUseCase.execute(params = action.route)
			.map { _ -> { state -> state } }
	}
}