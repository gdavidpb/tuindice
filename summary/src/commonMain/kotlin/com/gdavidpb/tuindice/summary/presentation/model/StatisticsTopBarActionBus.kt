package com.gdavidpb.tuindice.summary.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Carries the top bar actions of the Statistics screen from the host to the screen, which is the one that opens
 * what they ask for. It keeps no action for a screen that is not listening: the bar only shows them while the screen
 * is in front.
 */
class StatisticsTopBarActionBus {
	private val mutableActions = MutableSharedFlow<TopBarAction>(extraBufferCapacity = 1)

	val actions: Flow<TopBarAction> = mutableActions.asSharedFlow()

	fun dispatch(action: TopBarAction) {
		mutableActions.tryEmit(action)
	}
}
