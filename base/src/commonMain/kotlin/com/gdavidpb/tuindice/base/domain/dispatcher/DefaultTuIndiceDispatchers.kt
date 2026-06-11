package com.gdavidpb.tuindice.base.domain.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DefaultTuIndiceDispatchers : TuIndiceDispatchers {
	override val main: CoroutineDispatcher = Dispatchers.Main
	override val default: CoroutineDispatcher = Dispatchers.Default
	override val io: CoroutineDispatcher = Dispatchers.Default
	override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
