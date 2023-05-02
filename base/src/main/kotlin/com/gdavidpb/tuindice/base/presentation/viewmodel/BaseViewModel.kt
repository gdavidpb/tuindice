package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.presentation.reducer.ViewAction
import com.gdavidpb.tuindice.base.presentation.reducer.ViewEvent
import com.gdavidpb.tuindice.base.presentation.reducer.ViewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : ViewState, Action : ViewAction, Event : ViewEvent>(
	val initialViewState: State
) : ViewModel() {
	private val stateFlow = MutableStateFlow(initialViewState)
	val viewState = stateFlow.asStateFlow()

	private val actionFlow = MutableSharedFlow<Action>()
	private val viewAction = actionFlow.asSharedFlow()

	private val eventChannel = Channel<Event>()
	val viewEvent = eventChannel.receiveAsFlow()

	init {
		viewModelScope.launch {
			viewAction
				.collect { action ->
					launch { reducer(action) }
				}
		}
	}

	protected abstract suspend fun reducer(action: Action)

	protected fun emitAction(action: Action) {
		viewModelScope.launch { actionFlow.emit(action) }
	}

	fun getCurrentState(): State {
		return viewState.value
	}

	fun setState(state: State) {
		stateFlow.value = state
	}

	fun sendEvent(event: Event) {
		viewModelScope.launch { eventChannel.send(event) }
	}
}