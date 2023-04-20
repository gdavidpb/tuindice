package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseProcessor
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

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
		.collect { state ->
			setState(state)
		}
}