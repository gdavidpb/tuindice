package com.gdavidpb.tuindice.testkit.coroutines

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import kotlinx.coroutines.CoroutineDispatcher

class TestTuIndiceDispatchers(
	private val dispatcher: CoroutineDispatcher
) : TuIndiceDispatchers {
	override val main: CoroutineDispatcher = dispatcher
	override val default: CoroutineDispatcher = dispatcher
	override val io: CoroutineDispatcher = dispatcher
	override val unconfined: CoroutineDispatcher = dispatcher
}
