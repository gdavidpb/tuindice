package com.gdavidpb.tuindice.base.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.flow.mapNotNull

abstract class BaseReducer<P, T, E, ViewState, ViewAction, ViewEvent> {
	protected abstract val useCase: FlowUseCase<P, T, E>

	protected abstract fun actionToParams(action: ViewAction): P

	protected abstract suspend fun reduceLoadingState(
		state: UseCaseState.Loading<T, E>,
		eventProducer: (ViewEvent) -> Unit
	): ViewState

	protected abstract suspend fun reduceDataState(
		state: UseCaseState.Data<T, E>,
		eventProducer: (ViewEvent) -> Unit
	): ViewState

	protected abstract suspend fun reduceErrorState(
		state: UseCaseState.Error<T, E>,
		eventProducer: (ViewEvent) -> Unit
	): ViewState

	open suspend fun reduce(
		action: ViewAction,
		stateProducer: (ViewState) -> Unit,
		eventProducer: (ViewEvent) -> Unit
	) {
		useCase.execute(params = actionToParams(action))
			.mapNotNull { state ->
				when (state) {
					is UseCaseState.Loading -> reduceLoadingState(state, eventProducer)
					is UseCaseState.Data -> reduceDataState(state, eventProducer)
					is UseCaseState.Error -> reduceErrorState(state, eventProducer)
					else -> null
				}
			}
			.collect { state ->
				stateProducer(state)
			}
	}
}