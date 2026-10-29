package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// What the queue says about its writes: pending from the moment a write is queued, until no write is queued or running.
@OptIn(ExperimentalCoroutinesApi::class)
class AssistantConsentWriteQueueContractTest {
	@Test
	fun pending_staysTrueUntilTheLastQueuedWriteEnds() = runTest {
		val queue = AssistantConsentWriteQueue(
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		)
		val firstGate = CompletableDeferred<Unit>()
		val secondGate = CompletableDeferred<Unit>()
		assertFalse(queue.hasPendingWrites)

		queue.enqueue { firstGate.await() }
		queue.enqueue { secondGate.await() }
		assertTrue(queue.hasPendingWrites)

		// The first write ends while the second one is queued: a write is still pending.
		firstGate.complete(Unit)
		testScheduler.advanceUntilIdle()
		assertTrue(queue.hasPendingWrites)

		secondGate.complete(Unit)
		testScheduler.advanceUntilIdle()
		assertFalse(queue.hasPendingWrites)
	}

	@Test
	fun pending_staysTrueWhileAnOlderWriteRuns_evenWhenANewerOneIsCancelled() = runTest {
		val queue = AssistantConsentWriteQueue(
			sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		)
		val olderGate = CompletableDeferred<Unit>()

		queue.enqueue { olderGate.await() }
		val newer = queue.enqueue { }

		// The newer write is cancelled while it waits for the older one, which is still running.
		newer.cancel()
		testScheduler.advanceUntilIdle()
		assertTrue(queue.hasPendingWrites)

		olderGate.complete(Unit)
		testScheduler.advanceUntilIdle()
		assertFalse(queue.hasPendingWrites)
	}

	@Test
	fun writeEnds_countsAWriteThatEndsBeforeItsEnqueueReturns() = runTest {
		// An unconfined session runs the write at once, so it has ended by the time enqueue returns: the end is still a
		// new value for whoever follows the queue.
		val queue = AssistantConsentWriteQueue(sessionCoroutineScope = testSessionCoroutineScope())
		val endsBefore = queue.writeEnds.value

		queue.enqueue { }

		assertFalse(queue.hasPendingWrites)
		assertTrue(queue.writeEnds.value > endsBefore)
	}

	@Test
	fun pending_settlesToFalseWhenTheSessionCancelsItsWrites() = runTest {
		val session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val queue = AssistantConsentWriteQueue(sessionCoroutineScope = session)

		queue.enqueue { CompletableDeferred<Unit>().await() }
		queue.enqueue { }
		assertTrue(queue.hasPendingWrites)

		// The session cancels the writes it runs, the held one included: the flag settles once they have all ended.
		session.cancelActiveWork()
		testScheduler.advanceUntilIdle()

		assertFalse(queue.hasPendingWrites)
	}
}
