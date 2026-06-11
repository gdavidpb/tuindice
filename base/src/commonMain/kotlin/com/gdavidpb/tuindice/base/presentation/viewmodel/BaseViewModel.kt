package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.eventName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
	name: String,
	initialState: S,
	initialAction: A? = null,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : ViewModel() {
	private val effectChannel = Channel<E>(Channel.BUFFERED)
	private val actionChannel = Channel<A>(Channel.BUFFERED)
	private var lastPublishedState: String? = null

	protected abstract val eventPublisher: EventPublisher

	private val sendEffect: (E) -> Unit = { viewEffect ->
		publishEffect(name, viewEffect)
		if (!effectChannel.trySend(viewEffect).isSuccess)
			viewModelScope.launch { effectChannel.send(viewEffect) }
	}

	val action = actionChannel.receiveAsFlow()
	val effect = effectChannel.receiveAsFlow()

	val state = action
		.onStart {
			publishScreenView(name)
			if (initialAction != null) emit(initialAction)
		}
		.onEach { action -> publishAction(name, action) }
		.flatMapMerge { action -> processAction(action, sendEffect) }
		.scan(initialState) { currentState, mutation -> mutation(currentState) }
		.distinctUntilChanged()
		.onEach { state -> publishState(name, state) }
		.flowOn(dispatchers.default)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
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

	private fun publishScreenView(name: String) {
		eventPublisher.publish(
			AppEvent.ScreenView(source = name)
		)
	}

	private fun publishAction(name: String, action: A) {
		eventPublisher.publish(
			AppEvent.Action(
				source = name,
				action = action.eventName()
			)
		)
	}

	private fun publishState(name: String, state: S) {
		val stateName = state.eventName()
		if (lastPublishedState == stateName) return

		lastPublishedState = stateName
		eventPublisher.publish(
			AppEvent.State(
				source = name,
				state = stateName
			)
		)
	}

	private fun publishEffect(name: String, viewEffect: E) {
		eventPublisher.publish(
			AppEvent.Effect(
				source = name,
				effect = viewEffect.eventName()
			)
		)
	}
}
