package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
	initialState: S,
	initialAction: A? = null
) : ViewModel() {
	private val effectChannel = Channel<E>(Channel.BUFFERED)
	private val actionChannel = Channel<A>(Channel.BUFFERED)

	val action = actionChannel.receiveAsFlow()
	val effect = effectChannel.receiveAsFlow()

	val state = action
		.onStart { if (initialAction != null) emit(initialAction) }
		.flatMapMerge { action -> processAction(action, ::sendEffect) }
		.scan(initialState) { currentState, mutation -> mutation(currentState) }
		.distinctUntilChanged()
		.flowOn(Dispatchers.Default)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
			initialValue = initialState
		)

	protected abstract suspend fun processAction(
		action: A,
		sideEffect: (E) -> Unit
	): Flow<Mutation<S>>

	protected fun sendAction(viewAction: A) {
		if (!actionChannel.trySend(viewAction).isSuccess)
			viewModelScope.launch { actionChannel.send(viewAction) }
	}

	private fun sendEffect(viewEffect: E) {
		if (!effectChannel.trySend(viewEffect).isSuccess)
			viewModelScope.launch { effectChannel.send(viewEffect) }
	}
}
