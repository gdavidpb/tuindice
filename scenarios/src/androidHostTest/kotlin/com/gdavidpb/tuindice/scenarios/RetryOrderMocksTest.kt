package com.gdavidpb.tuindice.scenarios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The stateful retry fixtures answer the same whichever request reaches the server first. A seeded session reads
 * before its sync can answer, and that order is not fixed, so each retry scenario is replayed against the
 * mapping files with the sync placed before, between and after the reads, and every request must get the
 * status the scenario waits for. The negative cases remove one stub and expect the replay to notice.
 */
class RetryOrderMocksTest {
	@Test
	fun theRecordRetryFailsTheReadOnceWhicheverComesFirstTheSyncOrTheRead() {
		ordersOf(reads = 2).forEach { order ->
			val replies = play(order, replay(RECORD_RETRY), ::recordRetryRequest)

			assertEquals(listOf(UNAVAILABLE, OK), replies.reads, "reads in order $order: ${replies.all}")
			assertTrue(replies.syncs.all { it == UNAVAILABLE }, "a sync in order $order was not 503: ${replies.all}")
		}
	}

	@Test
	fun theSummaryRetryFailsTheFirstUserReadAndServesTheSecondWhicheverComesFirstTheSyncOrTheRead() {
		ordersOf(reads = 2).forEach { order ->
			val replies = play(order, replay(SUMMARY_RETRY), ::summaryRetryRequest)

			assertEquals(listOf(UNAVAILABLE, OK), replies.reads, "reads in order $order: ${replies.all}")
			assertTrue(replies.syncs.all { it == UNAVAILABLE }, "a sync in order $order was not 503: ${replies.all}")
		}
	}

	@Test
	fun withoutTheStubThatFailsTheReadFromTheStartTheReadFirstOrderGetsContent() {
		val broken = play(
			"GSG",
			replay(RECORD_RETRY, without = "get-record-refresh-retry-unavailable-once-from-start.json"),
			::recordRetryRequest
		)

		assertEquals(OK, broken.reads.first(), "the replay should see the first read answered with content")
	}

	@Test
	fun aStateWithoutASyncStubIsAnswered404SoTheReplayWouldCatchIt() {
		val broken = play(
			"SSGG",
			replay(RECORD_RETRY, without = "post-sync-record-refresh-retry-unavailable-while-unavailable.json"),
			::recordRetryRequest
		)

		assertTrue(NOT_FOUND in broken.syncs, "the replay should see an unanswered sync: ${broken.all}")
	}

	/** The mappings of the given WireMock scenarios plus the ones with no scenario, as the server holds them. */
	private fun replay(vararg scenarios: String, without: String? = null): MockReplay =
		MockReplay.of(*scenarios, without = without)

	/** Every placement of one sync among [reads] reads (before, between, after), and two syncs together. */
	private fun ordersOf(reads: Int): List<String> {
		val reading = "G".repeat(reads)
		val single = (0..reads).map { reading.substring(0, it) + "S" + reading.substring(it) }

		return single + listOf("SS$reading", reading.take(1) + "SS" + reading.drop(1))
	}

	private fun play(order: String, replay: MockReplay, request: (MockReplay, Char) -> MockReplay.Reply): Replies =
		Replies(order.map { event -> event to request(replay, event) })

	private fun recordRetryRequest(replay: MockReplay, event: Char): MockReplay.Reply {
		val bearer = mapOf("Authorization" to "Bearer record.refresh.retry.mock.access")

		return if (event == 'S') {
			replay.send("POST", "/record/v5/sync", bearer, body = mapOf("password" to "record-retry-pass"))
		} else {
			replay.send("GET", "/record/v5", bearer)
		}
	}

	private fun summaryRetryRequest(replay: MockReplay, event: Char): MockReplay.Reply {
		val bearer = mapOf("Authorization" to "Bearer summary.refresh.retry.mock.access")

		return if (event == 'S') {
			replay.send("POST", "/record/v5/sync", bearer, body = mapOf("password" to "summary-retry-pass"))
		} else {
			replay.send("GET", "/users/v1", bearer)
		}
	}

	private class Replies(val all: List<Pair<Char, MockReplay.Reply>>) {
		val reads: List<Int> get() = all.filter { it.first == 'G' }.map { it.second.status }
		val syncs: List<Int> get() = all.filter { it.first == 'S' }.map { it.second.status }
	}

	private companion object {
		const val RECORD_RETRY = "record-refresh-retry"
		const val SUMMARY_RETRY = "summary-refresh-retry"
		const val OK = 200
		const val UNAVAILABLE = 503
		const val NOT_FOUND = 404
	}
}
