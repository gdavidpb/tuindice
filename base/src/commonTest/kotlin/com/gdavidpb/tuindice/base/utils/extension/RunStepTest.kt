package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RunStepTest {
	@Test
	fun aStepThatSucceeds_returnsNoFailure() = runTest {
		assertEquals(null, runStep { })
	}

	@Test
	fun aStepThatFails_returnsItsFailure() = runTest {
		val failure = IllegalStateException("broken")

		assertSame(failure, runStep { throw failure })
	}

	// A CancellationException is not always the cancellation of the job (a timeout inside the step, a child
	// it waited for): it is one more failure of the step, and the job goes on to the next one.
	@Test
	fun aCancellationThatIsNotTheJobs_isOneMoreFailureOfTheStep() = runTest {
		var wentOn = false
		var failure: Throwable? = null

		val job = launch {
			failure = runStep { throw CancellationException("not the job's") }
			wentOn = true
		}
		advanceUntilIdle()

		assertTrue(failure is CancellationException)
		assertTrue(wentOn, "the next step runs")
		assertFalse(job.isCancelled)
	}

	// The cancellation of the job itself is not one more failure: the job that runs the steps stops.
	@Test
	fun aJobThatWasCancelled_isStoppedByTheStep() = runTest {
		var wentOn = false

		val job = launch {
			runStep {
				currentCoroutineContext().cancel()
				yield()
			}
			wentOn = true
		}
		advanceUntilIdle()

		assertTrue(job.isCancelled)
		assertFalse(wentOn, "a cancelled job does not go on to the next step")
	}
}
