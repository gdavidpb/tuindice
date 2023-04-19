package com.gdavidpb.tuindice.base.utils.extension

import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseProcessor
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn

suspend fun <P, T, E, ViewState, ViewEvent, U : FlowUseCase<P, T, E>> BaseViewModel<ViewState, *, ViewEvent>.execute(
	useCase: U,
	params: P,
	processor: BaseProcessor<T, E, ViewState, ViewEvent>
) {
	useCase.execute(params)
		.mapNotNull { state ->
			when (state) {
				is UseCaseState.Loading -> processor.processLoadingState(state)
				is UseCaseState.Data -> processor.processDataState(state)
				is UseCaseState.Error -> processor.processErrorState(state)
				else -> null
			}
		}
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
			initialValue = currentState
		)
		.collect { state ->
			setState(state)
		}
}