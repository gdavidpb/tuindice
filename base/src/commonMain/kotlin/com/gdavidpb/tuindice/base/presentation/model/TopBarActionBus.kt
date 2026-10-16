package com.gdavidpb.tuindice.base.presentation.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Carries the top bar actions of one screen from the host to that screen, which is the one that opens what
 * they ask for. It keeps no action for a screen that is not listening: the bar only shows them while the
 * screen is in front. Each screen declares its own subclass, so that Koin tells the buses of two screens apart.
 */
open class TopBarActionBus<T> {
	private val mutableActions = MutableSharedFlow<T>(extraBufferCapacity = 1)

	val actions: Flow<T> = mutableActions.asSharedFlow()

	fun dispatch(action: T) {
		mutableActions.tryEmit(action)
	}
}
