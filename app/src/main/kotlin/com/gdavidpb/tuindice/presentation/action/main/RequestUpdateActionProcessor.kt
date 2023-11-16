package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RequestUpdateActionProcessor(
	private val getUpdateInfoUseCase: GetUpdateInfoUseCase
) : ActionProcessor<Main.State, Main.Action.RequestUpdate, Main.Effect>() {

	override fun process(
		action: Main.Action.RequestUpdate,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return getUpdateInfoUseCase.execute(params = action.appUpdateManager)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						state
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Main.Effect.StartUpdateFlow(updateInfo = useCaseState.value)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						state
					}
				}
			}
	}
}