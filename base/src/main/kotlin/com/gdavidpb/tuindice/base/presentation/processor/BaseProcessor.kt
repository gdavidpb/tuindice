package com.gdavidpb.tuindice.base.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState

abstract class BaseProcessor<T, E, ViewState, ViewEvent>(
	open val eventChannel: (ViewEvent) -> Unit
) {
	abstract suspend fun processLoadingState(state: UseCaseState.Loading<T, E>): ViewState
	abstract suspend fun processDataState(state: UseCaseState.Data<T, E>): ViewState
	abstract suspend fun processErrorState(state: UseCaseState.Error<T, E>): ViewState
}