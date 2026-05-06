package com.gdavidpb.tuindice.pensum.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PensumTopBarActionBus {
	private val mutableActions = MutableSharedFlow<TopBarAction>(extraBufferCapacity = 1)

	val actions: Flow<TopBarAction> = mutableActions.asSharedFlow()

	fun dispatch(action: TopBarAction) {
		mutableActions.tryEmit(action)
	}
}
