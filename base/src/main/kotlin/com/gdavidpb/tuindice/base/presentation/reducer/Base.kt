package com.gdavidpb.tuindice.base.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow

interface ViewAction : ViewInput
interface ViewState : ViewOutput
interface ViewEvent : ViewOutput

interface ViewInput
interface ViewOutput

suspend inline fun <reified State : ViewState, reified Event : ViewEvent, T, E> Flow<UseCaseState<T, E>>.collect(
	viewModel: BaseViewModel<State, *, Event>,
	reducer: BaseReducer<State, Event, T, E>
) = reducer
	.reduce(this, viewModel::getCurrentState)
	.collect { output ->
		when (output) {
			is State -> viewModel.setState(output)
			is Event -> viewModel.sendEvent(output)
		}
	}