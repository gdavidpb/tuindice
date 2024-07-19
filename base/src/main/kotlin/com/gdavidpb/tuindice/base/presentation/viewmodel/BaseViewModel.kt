package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.waitForSubscribers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
	initialState: S,
	initialAction: A? = null
) : ViewModel() {
	val action = MutableSharedFlow<A>()
	val effect = MutableSharedFlow<E>()
	val state = action
		.onStart { if (initialAction != null) emit(initialAction) }
		.flatMapMerge { action -> processAction(action, ::sendEffect) }
		.scan(initialState) { currentState, mutation -> mutation(currentState) }
		.distinctUntilChanged()
		.flowOn(Dispatchers.IO)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
			initialValue = initialState
		)

	protected abstract fun processAction(
		action: A,
		sideEffect: (E) -> Unit
	): Flow<Mutation<S>>

	protected fun sendAction(viewAction: A) {
		viewModelScope.launch {
			action.waitForSubscribers()
			action.emit(viewAction)
		}
	}

	private fun sendEffect(viewEffect: E) {
		viewModelScope.launch {
			effect.waitForSubscribers()
			effect.emit(viewEffect)
		}
	}
}