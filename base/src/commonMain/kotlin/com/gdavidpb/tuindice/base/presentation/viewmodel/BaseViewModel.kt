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
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
	private val name: String,
	initialState: S,
	initialAction: A? = null,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : ViewModel() {
	private val effectChannel = Channel<E>(Channel.UNLIMITED)
	private val actionChannel = Channel<A>(Channel.UNLIMITED)
	private var lastPublishedState: String? = null

	protected abstract val eventPublisher: EventPublisher

	val effect = effectChannel.receiveAsFlow()

	val state = actionChannel.receiveAsFlow()
		.onStart {
			eventPublisher.publish(AppEvent.ScreenView(source = name))
			if (initialAction != null) emit(initialAction)
		}
		.onEach { action ->
			eventPublisher.publish(AppEvent.Action(source = name, action = action.eventName()))
		}
		.flatMapMerge { action -> processAction(action, ::sendEffect) }
		.scan(initialState) { currentState, mutation -> mutation(currentState) }
		.onEach(::publishState)
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
		actionChannel.trySend(viewAction)
	}

	private fun sendEffect(viewEffect: E) {
		val event = AppEvent.Effect(
			source = name,
			effect = viewEffect.eventName()
		)

		eventPublisher.publish(event)
		effectChannel.trySend(viewEffect)
	}

	private fun publishState(state: S) {
		val stateName = state.eventName()
		if (lastPublishedState == stateName) return

		lastPublishedState = stateName

		val event = AppEvent.State(
			source = name,
			state = stateName
		)

		eventPublisher.publish(event)
	}
}
