package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
abstract class BaseViewModel<ViewState, ViewAction, ViewEvent>(
	val initialViewState: ViewState
) : ViewModel() {
	val currentState: ViewState
		get() = viewState.value

	private val stateFlow = MutableStateFlow(initialViewState)
	val viewState = stateFlow.asStateFlow()

	private val actionFlow = MutableSharedFlow<ViewAction>()
	private val viewAction = actionFlow.asSharedFlow()

	private val eventChannel = Channel<ViewEvent>()
	val viewEvent = eventChannel.receiveAsFlow()

	init {
		viewModelScope.launch {
			viewAction
				.sample(500L)
				.collect { action ->
					launch { reducer(currentState, action) }
				}
		}
	}

	protected abstract suspend fun reducer(currentState: ViewState, action: ViewAction)

	protected fun setState(state: ViewState) {
		stateFlow.value = state
	}

	protected fun emitAction(action: ViewAction) {
		viewModelScope.launch { actionFlow.emit(action) }
	}

	protected fun sendEvent(event: ViewEvent) {
		viewModelScope.launch { eventChannel.send(event) }
	}
}