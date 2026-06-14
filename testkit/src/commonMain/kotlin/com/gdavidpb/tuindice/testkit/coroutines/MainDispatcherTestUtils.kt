package com.gdavidpb.tuindice.testkit.coroutines

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun TestScope.withMainDispatcher(
	dispatcher: TestDispatcher = UnconfinedTestDispatcher(testScheduler),
	block: suspend TestScope.(TuIndiceDispatchers) -> Unit
) {
	Dispatchers.setMain(dispatcher)
	try {
		block(TestTuIndiceDispatchers(dispatcher))
	} finally {
		Dispatchers.resetMain()
	}
}

/**
 * [withMainDispatcher] with the dispatcher fixed to an [UnconfinedTestDispatcher] bound
 * to this scope's scheduler, so the dispatcher driving Dispatchers.Main and the
 * [TuIndiceDispatchers] handed to [block] can never diverge.
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun TestScope.withUnconfinedTestDispatchers(
	block: suspend TestScope.(TuIndiceDispatchers) -> Unit
) {
	withMainDispatcher(UnconfinedTestDispatcher(testScheduler), block)
}
