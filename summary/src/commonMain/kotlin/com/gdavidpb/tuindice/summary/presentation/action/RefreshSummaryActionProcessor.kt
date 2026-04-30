package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateUserUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart

class RefreshSummaryActionProcessor(
	private val updateUserUseCase: UpdateUserUseCase
) : ActionProcessor<Summary.State, Summary.Action.RefreshSummary, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.RefreshSummary,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return updateUserUseCase.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> null

					is UseCaseState.Data -> suspend { state: Summary.State ->
						when (state) {
							is Summary.State.Content ->
								state.copy(isUserRefreshing = false)

							Summary.State.Idle ->
								state

							is Summary.State.Loading ->
								state.copy(isUserRefreshing = false)

							is Summary.State.Failed ->
								state.copy(isUserRefreshing = false)
						}
					}

					is UseCaseState.Error -> when (useCaseState.error) {
						is UpdateUserUseCaseError.NoConnection,
						is UpdateUserUseCaseError.Timeout,
						is UpdateUserUseCaseError.Unavailable,
						UpdateUserUseCaseError.NotFound,
						null,
						-> suspend { state: Summary.State ->
							when (state) {
								is Summary.State.Content ->
									state.copy(isUserRefreshing = false)

								Summary.State.Idle,
								is Summary.State.Loading,
								is Summary.State.Failed,
								-> Summary.State.Failed()
							}
						}
					}
				}
			}
			.onStart {
				emit(
					suspend { state: Summary.State ->
						when (state) {
							is Summary.State.Content ->
								state.copy(isUserRefreshing = true)

							Summary.State.Idle ->
								Summary.State.Loading(isUserRefreshing = true)

							is Summary.State.Loading ->
								state.copy(isUserRefreshing = true)

							is Summary.State.Failed ->
								Summary.State.Loading(isUserRefreshing = true)
						}
					}
				)
			}
	}
}
