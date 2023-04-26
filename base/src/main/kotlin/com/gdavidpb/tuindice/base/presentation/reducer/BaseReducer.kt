package com.gdavidpb.tuindice.base.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull

abstract class BaseReducer<P, T, E, ViewState, ViewAction, ViewEvent> {
	protected abstract val useCase: FlowUseCase<P, T, E>

	protected abstract fun actionToParams(action: ViewAction): P

	protected open suspend fun reduceLoadingState(
		currentState: ViewState,
		useCaseState: UseCaseState.Loading<T, E>,
		eventProducer: (ViewEvent) -> Unit
	): ViewState? = null

	protected open suspend fun reduceDataState(
		currentState: ViewState,
		useCaseState: UseCaseState.Data<T, E>,
		eventProducer: (ViewEvent) -> Unit
	): ViewState? = null

	protected open suspend fun reduceErrorState(
		currentState: ViewState,
		useCaseState: UseCaseState.Error<T, E>,
		eventProducer: (ViewEvent) -> Unit
	): ViewState? = null

	open suspend fun reduce(
		action: ViewAction,
		currentState: () -> ViewState,
		stateProducer: (ViewState) -> Unit,
		eventProducer: (ViewEvent) -> Unit
	) {
		val params = runCatching { actionToParams(action) }.getOrNull()

		if (params == null) {
			reportUnrecoverableException(NullPointerException()); return
		}

		useCase.execute(params)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						reduceLoadingState(
							currentState(),
							useCaseState,
							eventProducer
						)

					is UseCaseState.Data ->
						reduceDataState(
							currentState(),
							useCaseState,
							eventProducer
						)

					is UseCaseState.Error ->
						reduceErrorState(
							currentState(),
							useCaseState,
							eventProducer
						)

					else -> null
				}
			}
			.catch { throwable ->
				reportUnrecoverableException(throwable)
			}
			.collect { state ->
				stateProducer(state)
			}
	}

	private fun reportUnrecoverableException(throwable: Throwable) {
		throwable.printStackTrace()
	}
}