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
