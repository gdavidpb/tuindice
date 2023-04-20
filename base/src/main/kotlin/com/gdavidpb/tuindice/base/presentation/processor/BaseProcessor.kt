package com.gdavidpb.tuindice.base.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState

abstract class BaseProcessor<T, E, V, ViewEvent>(
	open val eventChannel: (ViewEvent) -> Unit
) {
	abstract suspend fun processDataState(state: UseCaseState.Data<T, E>): V
	abstract suspend fun processLoadingState(state: UseCaseState.Loading<T, E>): V
	abstract suspend fun processErrorState(state: UseCaseState.Error<T, E>): V
}