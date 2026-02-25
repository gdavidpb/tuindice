package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalAtomicApi::class)
abstract class BaseViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
	initialState: S,
	initialAction: A? = null
) : ViewModel() {
	private val initialActionLocker = AtomicBoolean(false)

	val action = MutableSharedFlow<A>()
	val effect = MutableSharedFlow<E>(
		extraBufferCapacity = 8,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)
	val state = action
		.onStart {
			if (initialAction != null && initialActionLocker.compareAndSet(expectedValue = false, newValue = true)) {
				emit(initialAction)
			}
		}
		.flatMapMerge { action -> processAction(action, ::sendEffect) }
		.scan(initialState) { currentState, mutation -> mutation(currentState) }
		.distinctUntilChanged()
		.flowOn(Dispatchers.Default)
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
		if (!action.tryEmit(viewAction)) {
			viewModelScope.launch {
				action.emit(viewAction)
			}
		}
	}

	private fun sendEffect(viewEffect: E) {
		if (!effect.tryEmit(viewEffect)) {
			viewModelScope.launch {
				effect.emit(viewEffect)
			}
		}
	}
}
