package com.gdavidpb.tuindice.base.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseReducer<State : ViewState, Event : ViewEvent, T, E> {
	protected open fun reduceUnrecoverableState(
		currentState: State,
		throwable: Throwable
	): Flow<ViewOutput> = flowOf()

	protected open suspend fun reduceLoadingState(
		currentState: State,
		useCaseState: UseCaseState.Loading<T, E>
	): Flow<ViewOutput> = flowOf()

	protected open suspend fun reduceDataState(
		currentState: State,
		useCaseState: UseCaseState.Data<T, E>
	): Flow<ViewOutput> = flowOf()

	protected open suspend fun reduceErrorState(
		currentState: State,
		useCaseState: UseCaseState.Error<T, E>
	): Flow<ViewOutput> = flowOf()

	suspend fun reduce(
		useCaseFlow: Flow<UseCaseState<T, E>>,
		stateProvider: () -> State
	): Flow<ViewOutput> {
		return useCaseFlow
			.flatMapConcat { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						reduceLoadingState(
							currentState = stateProvider(),
							useCaseState = useCaseState
						)

					is UseCaseState.Data ->
						reduceDataState(
							currentState = stateProvider(),
							useCaseState = useCaseState
						)

					is UseCaseState.Error ->
						reduceErrorState(
							currentState = stateProvider(),
							useCaseState = useCaseState
						)
				}
			}
	}
}