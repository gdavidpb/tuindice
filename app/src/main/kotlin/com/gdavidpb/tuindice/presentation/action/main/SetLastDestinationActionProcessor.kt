package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetLastDestinationActionProcessor(
	private val setLastDestinationUseCase: SetLastDestinationUseCase
) : ActionProcessor<Main.State, Main.Action.SetLastDestination, Main.Effect>() {

	override fun process(
		action: Main.Action.SetLastDestination,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return setLastDestinationUseCase.execute(params = action.destination)
			.map { _ -> { state -> state } }
	}
}