package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

abstract class BaseViewModel<ViewState, ViewAction, ViewEvent>(
	val initialViewState: ViewState
) : ViewModel() {
	val currentState: ViewState
		get() = viewState.value

	private val stateFlow: MutableStateFlow<ViewState> = MutableStateFlow(initialViewState)
	val viewState = stateFlow.asStateFlow()

	private val actionFlow: MutableSharedFlow<ViewAction> = MutableSharedFlow()
	private val viewAction = actionFlow.asSharedFlow()

	private val eventChannel = Channel<ViewEvent>()
	val viewEvent = eventChannel.receiveAsFlow()

	init {
		viewModelScope.launch {
			viewAction
				.shareIn(
					scope = viewModelScope,
					started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
				)
				.collect { action ->
					handleAction(action)
				}
		}
	}

	protected abstract suspend fun handleAction(action: ViewAction)

	fun setState(state: ViewState) {
		stateFlow.value = state
	}

	fun emitAction(action: ViewAction) {
		viewModelScope.launch { actionFlow.emit(action) }
	}

	fun sendEvent(event: ViewEvent) {
		viewModelScope.launch { eventChannel.send(event) }
	}
}