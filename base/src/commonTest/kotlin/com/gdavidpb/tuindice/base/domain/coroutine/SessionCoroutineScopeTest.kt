package com.gdavidpb.tuindice.base.domain.coroutine

import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SessionCoroutineScopeTest {
	@Test
	fun cancelActiveWork_cancelsChildrenAndKeepsScopeReusable() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		var firstJobCancelled = false
		var secondJobCompleted = false

		val firstJob = scope.launch {
			try {
				awaitCancellation()
			} finally {
				firstJobCancelled = true
			}
		}
		runCurrent()

		scope.cancelActiveWork()
		runCurrent()

		val secondJob = scope.launch {
			secondJobCompleted = true
		}
		runCurrent()

		assertTrue(firstJob.isCancelled)
		assertTrue(firstJobCancelled)
		assertTrue(secondJob.isCompleted)
		assertTrue(secondJobCompleted)
	}
}
