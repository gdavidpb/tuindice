package com.gdavidpb.tuindice.base.domain.coroutine

import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
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

	// An invalidation met by a request that runs inside the scope (the sync, a revalidation) calls
	// cancelActiveWork from one of the scope's own children: the caller must not wait for itself, and
	// what it does next (wiping the session) must run.
	@Test
	fun cancelActiveWork_calledFromAChild_doesNotCancelNorAwaitTheCaller() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		var afterCancelRan = false
		var callerCancelled = false

		val sibling = scope.launch { awaitCancellation() }
		val caller = scope.launch {
			try {
				scope.cancelActiveWork()
				afterCancelRan = true
				yield()
			} catch (cancellation: CancellationException) {
				callerCancelled = true
				throw cancellation
			}
		}
		runCurrent()
		caller.join()

		assertTrue(afterCancelRan, "the code after cancelActiveWork runs in the caller")
		assertFalse(callerCancelled, "the caller is not cancelled by its own call")
		assertTrue(caller.isCompleted && !caller.isCancelled)
		assertTrue(sibling.isCancelled, "the other children are cancelled")
	}

	@Test
	fun cancelActiveWork_calledFromAGrandchild_doesNotCancelTheCaller() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		var afterCancelRan = false

		val parent = scope.launch {
			launch {
				scope.cancelActiveWork()
				afterCancelRan = true
			}
		}
		runCurrent()
		parent.join()

		assertTrue(afterCancelRan)
		assertFalse(parent.isCancelled)
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

	// The wipe cancels the caller's branch as its last step, so cancelActiveWork hands it back.
	@Test
	fun cancelActiveWork_returnsTheBranchOfTheCaller_andNullFromOutside() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val scope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
		var fromBranch: Job? = null
		var fromGrandchild: Job? = null

		val branch = scope.launch {
			fromBranch = scope.cancelActiveWork()
			launch { fromGrandchild = scope.cancelActiveWork() }
		}
		runCurrent()
		branch.join()

		assertSame(branch, fromBranch)
		assertSame(branch, fromGrandchild)
		assertNull(scope.cancelActiveWork())
	}

	// A child that relaunches work every time it is cancelled must not keep the sign-out spinning.
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
