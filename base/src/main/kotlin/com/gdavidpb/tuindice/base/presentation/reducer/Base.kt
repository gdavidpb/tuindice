package com.gdavidpb.tuindice.base.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow

interface ViewInput
interface ViewOutput

interface ViewState : ViewOutput
interface ViewAction : ViewInput
interface ViewEvent : ViewOutput

suspend inline fun <reified State : ViewState, reified Event : ViewEvent, T, E> Flow<UseCaseState<T, E>>.collect(
	viewModel: BaseViewModel<State, *, Event>,
	reducer: BaseReducer<State, Event, T, E>
) {
	reducer
		.reduce(useCaseFlow = this, stateProvider = viewModel::getCurrentState)
		.collect { output ->
			when (output) {
				is State -> viewModel.setState(output)
				is Event -> viewModel.sendEvent(output)
			}
		}
}