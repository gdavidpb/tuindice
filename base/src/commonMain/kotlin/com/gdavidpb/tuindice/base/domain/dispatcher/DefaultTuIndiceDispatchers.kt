package com.gdavidpb.tuindice.base.domain.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DefaultTuIndiceDispatchers : TuIndiceDispatchers {
	override val main: CoroutineDispatcher = Dispatchers.Main
	override val default: CoroutineDispatcher = Dispatchers.Default

	// A9: `io` is an alias of `Default` today, and no production code reads it (only the test
	// doubles implement it). Point it at a real IO dispatcher before the first blocking consumer.
	override val io: CoroutineDispatcher = Dispatchers.Default
	override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
