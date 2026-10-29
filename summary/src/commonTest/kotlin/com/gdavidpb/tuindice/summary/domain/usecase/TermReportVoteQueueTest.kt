package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class TermReportVoteQueueTest {
	@Test
	fun aVoteThatEnds_leavesNoReportTracked() = runTest {
		val queue = TermReportVoteQueue()

		queue.enqueue(scope = testSessionCoroutineScope(), insightId = "term-1:2") { }.await()

		assertEquals(0, queue.trackedReportCount())
	}

	@Test
	fun aVoteThatFails_leavesNoReportTracked() = runTest {
		val queue = TermReportVoteQueue()

		val vote = queue.enqueue(scope = testSessionCoroutineScope(), insightId = "term-1:2") {
			error("refused")
		}
		runCatching { vote.await() }

		assertEquals(0, queue.trackedReportCount())
	}

	@Test
	fun votesOfOneReport_keepTheReportTracked_untilTheLastOneEnds() = runTest {
		val queue = TermReportVoteQueue()
		val scope = testSessionCoroutineScope()
		val first = CompletableDeferred<Unit>()
		val second = CompletableDeferred<Unit>()

		val firstVote = queue.enqueue(scope = scope, insightId = "term-1:2") { first.await() }
		val secondVote = queue.enqueue(scope = scope, insightId = "term-1:2") { second.await() }
		assertEquals(1, queue.trackedReportCount())

		first.complete(Unit)
		firstVote.await()

		// The first vote is not the last of its report: the second one is still the one tracked.
		assertEquals(1, queue.trackedReportCount())

		second.complete(Unit)
		secondVote.await()

		assertEquals(0, queue.trackedReportCount())
	}

	// A vote that ends while a newer one of its report is queued must not free that newer one: a third vote queued after
	// the first one ended still waits for the second one.
	@Test
	fun aVoteEndingWhileANewerOneIsQueued_doesNotLetTheNextOneSkipTheWait() = runTest {
		val queue = TermReportVoteQueue()
		val scope = testSessionCoroutineScope()
		val started = mutableListOf<String>()
		val first = CompletableDeferred<Unit>()
		val second = CompletableDeferred<Unit>()

		val firstVote = queue.enqueue(scope = scope, insightId = "term-1:2") {
			started += "first"
			first.await()
		}
		val secondVote = queue.enqueue(scope = scope, insightId = "term-1:2") {
			started += "second"
			second.await()
		}

		first.complete(Unit)
		firstVote.await()

		val thirdVote = queue.enqueue(scope = scope, insightId = "term-1:2") { started += "third" }
		yield()

		assertEquals(listOf("first", "second"), started)

		second.complete(Unit)
		secondVote.await()
		thirdVote.await()

		assertEquals(listOf("first", "second", "third"), started)
	}

	@Test
	fun votesOfTwoReports_leaveNothingTrackedOnceTheyEnd() = runTest {
		val queue = TermReportVoteQueue()
		val scope = testSessionCoroutineScope()

		queue.enqueue(scope = scope, insightId = "term-1:2") { }.await()
		queue.enqueue(scope = scope, insightId = "term-2:1") { }.await()

		assertEquals(0, queue.trackedReportCount())
	}
}
