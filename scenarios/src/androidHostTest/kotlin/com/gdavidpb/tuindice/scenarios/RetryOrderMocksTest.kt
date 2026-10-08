package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.string
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
	private val mappingFiles = RepoFiles.allMappings.walkTopDown().filter { it.isFile && it.extension == "json" }
		.sortedBy { it.path }.toList()

	@Test
	fun theRecordRetryFailsTheReadOnceWhicheverComesFirstTheSyncOrTheRead() {
		ordersOf(reads = 2).forEach { order ->
			val replies = play(order, replay(RECORD_RETRY), ::recordRetryRequest)

			assertEquals(listOf(UNAVAILABLE, OK), replies.reads, "reads in order $order: ${replies.all}")
			assertTrue(replies.syncs.all { it == UNAVAILABLE }, "a sync in order $order was not 503: ${replies.all}")
		}
	}

	@Test
	fun theSummaryRetryFailsTheUserReadAsManyTimesAsEachPlatformNeedsInEveryOrder() {
		val expectedByPlatform = mapOf(
			"iOS" to listOf(UNAVAILABLE, OK),
			"Android" to listOf(UNAVAILABLE, OK)
		)

		expectedByPlatform.forEach { (platform, expected) ->
			ordersOf(reads = expected.size).forEach { order ->
				val replies = play(order, replay(SUMMARY_RETRY)) { replay, event ->
					summaryRetryRequest(replay, event, platform)
				}

				assertEquals(expected, replies.reads, "$platform reads in order $order: ${replies.all}")
				assertTrue(replies.syncs.all { it == UNAVAILABLE }, "$platform sync in $order was not 503: ${replies.all}")
			}
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

	@Test
	fun theOlderPensumIsServedByItsOwnMappingEvenAfterTheCachedRefreshStartedFailing() {
		val replay = replay(PENSUM_CACHE)
		val bearer = mapOf("Authorization" to "Bearer pensum.cache.mock.access")
		val older = mapOf("year" to "2018", "modality_id" to "long_internship")

		assertEquals(OK, replay.send("GET", "/pensums/v4", bearer).status, "the first read primes the cache")
		assertEquals(UNAVAILABLE, replay.send("GET", "/pensums/v4", bearer).status, "the refresh of the current pensum")
		assertEquals(
			MockReplay.Reply(OK, "get-pensum-2018-long_internship.json"),
			replay.send("GET", "/pensums/v4", bearer, older),
			"the older pensum must not tie with the failing refresh"
		)
	}

	/** The mappings of one WireMock scenario plus the ones with no scenario, as the server holds them. */
	private fun replay(scenario: String, without: String? = null): MockReplay =
		MockReplay(
			mappingFiles.filter { it.name != without }.map { it.name to MockJson.obj(it) }
				.filter { (_, mapping) -> mapping.string("scenarioName").let { it == null || it == scenario } }
		)

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
			replay.send("POST", "/record/v5/sync", bearer, password = "record-retry-pass")
		} else {
			replay.send("GET", "/record/v5", bearer)
		}
	}

	private fun summaryRetryRequest(replay: MockReplay, event: Char, platform: String): MockReplay.Reply {
		val headers = mapOf(
			"Authorization" to "Bearer summary.refresh.retry.mock.access",
			"User-Agent" to "TuIndice/6.4.0 ($platform)"
		)

		return if (event == 'S') {
			replay.send("POST", "/record/v5/sync", headers, password = "summary-retry-pass")
		} else {
			replay.send("GET", "/users/v1", headers)
		}
	}

	private class Replies(val all: List<Pair<Char, MockReplay.Reply>>) {
		val reads: List<Int> get() = all.filter { it.first == 'G' }.map { it.second.status }
		val syncs: List<Int> get() = all.filter { it.first == 'S' }.map { it.second.status }
	}

	private companion object {
		const val PENSUM_CACHE = "pensum-cache-refresh-failed"
		const val RECORD_RETRY = "record-refresh-retry"
		const val SUMMARY_RETRY = "summary-refresh-retry"
		const val OK = 200
		const val UNAVAILABLE = 503
		const val NOT_FOUND = 404
	}
}
