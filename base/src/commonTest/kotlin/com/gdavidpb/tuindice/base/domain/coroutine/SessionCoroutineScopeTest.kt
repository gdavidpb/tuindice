package com.gdavidpb.tuindice.base.domain.coroutine

import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

	// Whoever ends a session does it from outside the scope, so a call from one of its own children is
	// not special: the child is cancelled with the rest, and what follows the call does not run.
	@Test
	fun cancelActiveWork_calledFromAChild_cancelsTheCallerLikeAnyOtherChild() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		var afterCancelRan = false

		val sibling = scope.launch { awaitCancellation() }
		val caller = scope.launch {
			scope.cancelActiveWork()
			afterCancelRan = true
		}
		runCurrent()
		caller.join()

		assertFalse(afterCancelRan, "the code after cancelActiveWork does not run in a cancelled caller")
		assertTrue(caller.isCancelled, "the caller is cancelled with the others")
		assertTrue(sibling.isCancelled, "the other children are cancelled")
	}

	@Test
	fun cancelActiveWork_calledFromAGrandchild_cancelsTheWholeBranch() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		var afterCancelRan = false

		val parent = scope.launch {
			launch {
				scope.cancelActiveWork()
				afterCancelRan = true
			}
			awaitCancellation()
		}
		runCurrent()
		parent.join()

		assertFalse(afterCancelRan)
		assertTrue(parent.isCancelled)
	}

	// A caller outside the scope is not affected by the call, and waits for the children.
	@Test
	fun cancelActiveWork_calledFromOutside_waitsForTheChildrenToEnd() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		val child = scope.launch { awaitCancellation() }
		runCurrent()

		scope.cancelActiveWork()

		assertTrue(child.isCompleted, "the call returns once the child has ended")
	}

	// A child that is launched while the others are being cancelled (a cleanup that starts more work)
	// must be cancelled too, or it lives on after the session is wiped.
	@Test
	fun cancelActiveWork_alsoCancelsAChildLaunchedWhileCancelling() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		var late: Job? = null

		scope.launch {
			try {
				awaitCancellation()
			} finally {
				late = scope.launch { awaitCancellation() }
			}
		}
		runCurrent()

		scope.cancelActiveWork()
		runCurrent()

		assertTrue(checkNotNull(late).isCancelled, "the child launched during the cancellation is cancelled")
		assertTrue(checkNotNull(late).isCompleted)
	}

	// A child that relaunches work every time it is cancelled must not keep the sign-out spinning.
	// A child that does not end when it is cancelled (a request inside a context that cannot be
	// cancelled) must not keep the sign-out waiting for it: past the bound it is reported, and the
	// caller goes on to wipe the session.
	@Test
	fun cancelActiveWork_doesNotWaitForeverForAChildThatIgnoresTheCancellation_andReports() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val reporting = RecordingReportingRepository()
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher), reporting)
		val neverEnds = CompletableDeferred<Unit>()
		val stubborn = scope.launch { withContext(NonCancellable) { neverEnds.await() } }
		val polite = scope.launch { awaitCancellation() }
		runCurrent()

		scope.cancelActiveWork()

		assertFalse(stubborn.isCompleted, "the child that ignores the cancellation is left running")
		assertTrue(polite.isCancelled, "the others were cancelled")
		assertEquals(1, reporting.loggedExceptions.size, "and the one left behind is reported")
		assertTrue(
			currentTime in 1..60_000L,
			"the wait ends at a bound well under the 90 seconds of a request, not after them"
		)

		neverEnds.complete(Unit)
		runCurrent()
		assertTrue(stubborn.isCompleted)
	}

	@Test
	fun cancelActiveWork_givesUpAfterAFewPasses_andReports() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val reporting = RecordingReportingRepository()
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher), reporting)
		var relaunches = 0
		var keepRelaunching = true

		fun relaunch() {
			scope.launch {
				try {
					awaitCancellation()
				} finally {
					relaunches++
					if (keepRelaunching) relaunch()
				}
			}
		}
		relaunch()
		runCurrent()

		scope.cancelActiveWork()

		assertEquals(8, relaunches, "one pass per cancellation, and no more than the bound")
		assertEquals(1, reporting.loggedExceptions.size)

		keepRelaunching = false
		scope.cancelActiveWork()
		assertEquals(1, reporting.loggedExceptions.size, "a scope that settles is not reported")
	}
}
