package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StartUpActionProcessor(
	private val startUpUseCase: StartUpUseCase
) : ActionProcessor<Main.State, Main.Action.StartUp, Main.Effect>() {

	override fun process(
		action: Main.Action.StartUp,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return startUpUseCase.execute(params = action.data)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Main.State.Starting
					}

					is UseCaseState.Data -> { _ ->
						with(useCaseState.value) {
							Main.State.Content(
								title = title,
								startDestination = startDestination,
								currentDestination = currentDestination,
								destinations = destinations,
								topBarConfig = topBarConfig
							)
						}
					}

					is UseCaseState.Error -> { _ ->
						when (val error = useCaseState.error) {
							is StartUpError.NoServices ->
								sideEffect(Main.Effect.ShowNoServicesDialog(status = error.status))

							else -> {}
						}

						Main.State.Failed
					}
				}
			}
	}
}