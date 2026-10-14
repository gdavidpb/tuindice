package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Runs every dispatcher inline, so a test drives a data source without a real main thread or pool. */
object UnconfinedTuIndiceDispatchers : TuIndiceDispatchers {
	override val main: CoroutineDispatcher = Dispatchers.Unconfined
	override val default: CoroutineDispatcher = Dispatchers.Unconfined
	override val io: CoroutineDispatcher = Dispatchers.Unconfined
	override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
