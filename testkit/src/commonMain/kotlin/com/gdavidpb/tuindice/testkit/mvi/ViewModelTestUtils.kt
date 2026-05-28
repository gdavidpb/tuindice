package com.gdavidpb.tuindice.testkit.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> CoroutineScope.launchStateCollector(
	flow: Flow<T>,
	testScheduler: TestCoroutineScheduler
): Job {
	return launch(UnconfinedTestDispatcher(testScheduler)) {
		flow.collect()
	}
}
