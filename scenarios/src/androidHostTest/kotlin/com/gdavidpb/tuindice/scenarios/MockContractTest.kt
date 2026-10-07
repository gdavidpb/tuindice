package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.engine.BackendEngine
import com.gdavidpb.tuindice.scenarios.MockJson.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the scenarios rely on in `mocks/`: protected endpoints demand a bearer, delays a flow waits on survive the
 * fast profile, the retry fixtures keep their matchers, and every stateful transformer can be reset by the
 * interpreter.
 */
class MockContractTest {
	private val protectedPath = Regex(
		"""^/(users/v1($|/)|messaging/v1$|record/v5($|/)|evaluations/v3($|/)""" +
			"""|enrollment-proof/v1$|subjects/v1($|/)|pensums/v4$)"""
	)
	private val mappingFiles = RepoFiles.allMappings.walkTopDown().filter { it.isFile && it.extension == "json" }
		.sortedBy { it.path }.toList()

	@Test
	fun thereAreMappingsToCheck() {
		assertTrue(mappingFiles.size > MINIMUM_MAPPINGS, "only ${mappingFiles.size} mappings found")
	}

	@Test
	fun protectedMappingsRequireABearerAuthorization() {
		val offenders = mappingFiles.filter { file ->
			val request = MockJson.obj(file)["request"] as? JsonObject
			val path = request?.string("urlPath").orEmpty()
			val matcher = (request?.get("headers") as? JsonObject)?.get("Authorization") as? JsonObject
			val expected = matcher?.string("equalTo") ?: matcher?.string("matches").orEmpty()

			protectedPath.containsMatchIn(path) && (matcher == null || !expected.startsWith("Bearer "))
		}

		assertTrue(offenders.isEmpty(), "protected mappings without a Bearer matcher: ${offenders.map { it.name }}")
	}

	@Test
	fun semanticDelaysDeclareWhatTheFastProfileKeeps() {
		val offenders = mappingFiles.filter { file ->
			val mapping = MockJson.obj(file)
			val delay = ((mapping["response"] as? JsonObject)?.get("fixedDelayMilliseconds") as? JsonPrimitive)?.doubleOrNull
			val fast = (mapping["metadata"] as? JsonObject)?.get("fastDelayMilliseconds")

			(delay != null && delay >= SEMANTIC_DELAY_MS && fast == null) ||
				(fast != null && (fast as? JsonPrimitive)?.doubleOrNull == null)
		}

		assertTrue(
			offenders.isEmpty(),
			"delays >= ${SEMANTIC_DELAY_MS}ms need numeric metadata.fastDelayMilliseconds: ${offenders.map { it.name }}"
		)
	}

	@Test
	fun theRetryAndRejectionFixturesKeepTheirMatchers() {
		expectations.forEach { (path, text) ->
			val file = RepoFiles.file("mocks/mappings/$path")

			assertTrue(file.isFile, "missing mapping $path")
			assertTrue(text in file.readText(), "$path no longer contains $text")
		}
	}

	@Test
	fun staleRetryMappingsStayDeleted() {
		staleMappings.forEach { path ->
			assertTrue(!RepoFiles.file("mocks/mappings/$path").exists(), "$path should not exist")
		}
	}

	@Test
	fun everyStatefulTransformerIsResetByTheInterpreter() {
		val resetPaths = BackendEngine.resetPaths.map { it.path }
		val stateful = RepoFiles.transformers.listFiles { file -> file.name.endsWith("ResponseTransformerFactory.kt") }
			.orEmpty().filter { Regex("""(?m)^\s*private (var|val) state""").containsMatchIn(it.readText()) }

		assertTrue(stateful.isNotEmpty(), "no stateful transformer found; the reset check would pass for nothing")

		stateful.forEach { transformer ->
			val resetPath = resetPathOf(transformer)

			assertTrue(resetPath != null, "${transformer.name} keeps a dataset but handles no reset request")
			assertTrue(
				mappingFiles.any { "\"urlPath\": \"$resetPath\"" in it.readText() },
				"${transformer.name} resets on $resetPath but no mapping exposes it"
			)
			assertTrue(
				resetPath in resetPaths,
				"${transformer.name} resets on $resetPath but BackendEngine.resetPaths never calls it"
			)
		}
	}

	@Test
	fun everyCustomResetPathBelongsToATransformerAndItsMappingAcceptsTheHarnessCredential() {
		val known = RepoFiles.transformers.listFiles { file -> file.name.endsWith("ResponseTransformerFactory.kt") }
			.orEmpty().mapNotNull { resetPathOf(it) }.toSet()

		BackendEngine.resetPaths.filterNot { it.path.startsWith("/__admin/") }.forEach { reset ->
			assertTrue(reset.path in known, "${reset.path} is reset by the interpreter but no transformer handles it")

			val mapping = mappingFiles.map { MockJson.obj(it) }
				.single { it.string("request", "urlPath") == reset.path }
			val matcher = (((mapping["request"] as JsonObject)["headers"] as JsonObject)["Authorization"]) as JsonObject
			val credential = checkNotNull(reset.authorization)

			assertTrue(
				matcher.string("equalTo") == credential || matcher.string("matches")?.toRegex()?.matches(credential) == true,
				"the mapping of ${reset.path} does not accept '$credential'"
			)
		}
	}

	@Test
	fun theResetListStartsWithTheAdminResetsAndThenTheTransformers() {
		assertEquals(
			listOf("/__admin/scenarios/reset", "/__admin/requests"),
			BackendEngine.resetPaths.map { it.path }.take(2)
		)
	}

	@Test
	fun theRecordRetryFailsTheReadOnceWhicheverComesFirstTheSyncOrTheRead() {
		ordersOf(reads = 2).forEach { order ->
			val replies = play(order, replay(RECORD_RETRY_SCENARIO), ::recordRetryRequest)

			assertEquals(listOf(UNAVAILABLE, OK), replies.reads, "reads in order $order: ${replies.all}")
			assertTrue(replies.syncs.all { it == UNAVAILABLE }, "a sync in order $order was not answered 503: ${replies.all}")
		}
	}

	@Test
	fun theSummaryRetryFailsTheUserReadAsManyTimesAsEachPlatformNeedsInEveryOrder() {
		mapOf("iOS" to listOf(UNAVAILABLE, OK), "Android" to listOf(UNAVAILABLE, UNAVAILABLE, OK)).forEach { (platform, expected) ->
			ordersOf(reads = expected.size).forEach { order ->
				val replies = play(order, replay(SUMMARY_RETRY_SCENARIO)) { replay, event ->
					summaryRetryRequest(replay, event, platform)
				}

				assertEquals(expected, replies.reads, "$platform reads in order $order: ${replies.all}")
				assertTrue(replies.syncs.all { it == UNAVAILABLE }, "$platform sync in order $order was not 503: ${replies.all}")
			}
		}
	}

	@Test
	fun withoutTheStubThatFailsTheReadFromTheStartTheReadFirstOrderGetsContent() {
		val broken = play("GSG", replay(RECORD_RETRY_SCENARIO, without = "get-record-refresh-retry-unavailable-once-from-start.json"), ::recordRetryRequest)

		assertEquals(OK, broken.reads.first(), "the replay should see the first read answered with content: ${broken.all}")
	}

	@Test
	fun aStateWithoutASyncStubIsAnswered404SoTheReplayWouldCatchIt() {
		val broken = play("SSGG", replay(RECORD_RETRY_SCENARIO, without = "post-sync-record-refresh-retry-unavailable-while-unavailable.json"), ::recordRetryRequest)

		assertTrue(NOT_FOUND in broken.syncs, "the replay should see an unanswered sync: ${broken.all}")
	}

	@Test
	fun theOlderPensumIsServedByItsOwnMappingEvenAfterTheCachedRefreshStartedFailing() {
		val replay = replay(PENSUM_CACHE_SCENARIO)
		val bearer = mapOf("Authorization" to "Bearer pensum.cache.mock.access")
		val older = mapOf("year" to "2018", "modality_id" to "long_internship")

		assertEquals(OK, replay.send("GET", "/pensums/v4", bearer).status, "the first read primes the cache")
		assertEquals(UNAVAILABLE, replay.send("GET", "/pensums/v4", bearer).status, "the refresh of the current pensum fails")
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

	/** `isResetRequest` matches `pathSegments == listOf("a", "b", "reset")`; that is the path it serves. */
	private fun resetPathOf(transformer: File): String? {
		val body = Regex("""fun isResetRequest[\s\S]*?\n\s*\n""").find(transformer.readText())?.value
		val segments = body?.let { Regex("""listOf\(([^)]*)\)""").find(it)?.groupValues?.get(1) }

		return segments?.let {
			Regex(""""([^"]*)"""").findAll(it).joinToString("/", prefix = "/") { segment -> segment.groupValues[1] }
		}
	}

	private companion object {
		const val MINIMUM_MAPPINGS = 100
		const val SEMANTIC_DELAY_MS = 5000.0
		const val PENSUM_CACHE_SCENARIO = "pensum-cache-refresh-failed"
		const val RECORD_RETRY_SCENARIO = "record-refresh-retry"
		const val SUMMARY_RETRY_SCENARIO = "summary-refresh-retry"
		const val OK = 200
		const val UNAVAILABLE = 503
		const val NOT_FOUND = 404
		const val UNAVAILABLE_BODY = "\"bodyFileName\": \"sync/post-sync-record-unavailable.json\""

		private const val SYNC_RETRY = "sync/post-sync-summary-refresh-retry-unavailable"
		private const val SUMMARY_USER = "summary/get-user-refresh-retry"
		private const val RECORD_RETRY = "record/get-record-refresh-retry"

		/** Mapping (relative to `mocks/mappings`) and a matcher it has to keep. */
		val expectations: List<Pair<String, String>> = listOf(
			"record/patch-synthetic-term-rejected.json" to "\"equalTo\": \"Bearer record.term.rejected.mock.access\"",
			"login/auth-record-term-rejected-exchange-success.json" to "\"access_token\": \"record.term.rejected.mock.access\"",
			"sync/post-sync-summary-refresh-retry-unavailable.json" to "\$[?(@.password == 'summary-retry-pass')]",
			"sync/post-sync-summary-refresh-retry-unavailable.json" to UNAVAILABLE_BODY,
			"sync/post-sync-summary-refresh-retry-unavailable.json" to "\"newScenarioState\": \"InitialSyncUnavailable\"",
			"sync/post-sync-record-term-rejected-success.json" to "\$[?(@.password == 'record-rejected-pass')]",
			"sync/post-sync-record-refresh-retry-unavailable.json" to "\$[?(@.password == 'record-retry-pass')]",
			"sync/post-sync-record-refresh-retry-unavailable.json" to UNAVAILABLE_BODY,
			"sync/post-sync-record-refresh-retry-unavailable.json" to "\"newScenarioState\": \"InitialSyncUnavailable\"",
			"$SUMMARY_USER-fails-once.json" to "\"Authorization\"",
			"$SUMMARY_USER-fails-android-first.json" to "\"Authorization\"",
			"$SUMMARY_USER-fails-android-second.json" to "\"Authorization\"",
			"$SUMMARY_USER-success.json" to "\"Authorization\"",
			"$SUMMARY_USER-success-ios.json" to "\"Authorization\"",
			"$SUMMARY_USER-fails-once.json" to "\"requiredScenarioState\": \"InitialSyncUnavailable\"",
			"$SUMMARY_USER-fails-once.json" to "\"newScenarioState\": \"FailedOnce\"",
			"$SUMMARY_USER-fails-once.json" to "\"contains\": \"iOS\"",
			"$SUMMARY_USER-fails-android-first.json" to "\"requiredScenarioState\": \"InitialSyncUnavailable\"",
			// Order independence: a seeded session asks for the user before it syncs, so the first failure also
			// starts from the initial state, and a sync that arrives after it is refused too.
			"$SUMMARY_USER-fails-once-from-start.json" to "\"equalTo\": \"Bearer summary.refresh.retry.mock.access\"",
			"$SUMMARY_USER-fails-android-first-from-start.json" to "\"equalTo\": \"Bearer summary.refresh.retry.mock.access\"",
			"$SUMMARY_USER-fails-once-from-start.json" to "\"requiredScenarioState\": \"Started\"",
			"$SUMMARY_USER-fails-once-from-start.json" to "\"newScenarioState\": \"FailedOnce\"",
			"$SUMMARY_USER-fails-android-first-from-start.json" to "\"requiredScenarioState\": \"Started\"",
			"$SUMMARY_USER-fails-android-first-from-start.json" to "\"newScenarioState\": \"AndroidFailedOnce\"",
			"$SYNC_RETRY-after-failure.json" to "\"requiredScenarioState\": \"FailedOnce\"",
			"$SYNC_RETRY-after-failure.json" to UNAVAILABLE_BODY,
			"$SYNC_RETRY-after-android-failure.json" to "\"requiredScenarioState\": \"AndroidFailedOnce\"",
			"$SYNC_RETRY-after-android-failure.json" to UNAVAILABLE_BODY,
			"$SUMMARY_USER-fails-android-first.json" to "\"newScenarioState\": \"AndroidFailedOnce\"",
			"$SUMMARY_USER-fails-android-first.json" to "\"contains\": \"Android\"",
			"$SUMMARY_USER-fails-android-second.json" to "\"requiredScenarioState\": \"AndroidFailedOnce\"",
			"$SUMMARY_USER-fails-android-second.json" to "\"newScenarioState\": \"FailedOnce\"",
			"$SUMMARY_USER-fails-android-second.json" to "\"contains\": \"Android\"",
			"$SUMMARY_USER-success.json" to "\"requiredScenarioState\": \"FailedOnce\"",
			"$SUMMARY_USER-success.json" to "\"contains\": \"Android\"",
			"$SUMMARY_USER-success-ios.json" to "\"requiredScenarioState\": \"FailedOnce\"",
			"$SUMMARY_USER-success-ios.json" to "\"contains\": \"iOS\"",
			"$RECORD_RETRY-unavailable-once.json" to "\"Authorization\"",
			"$RECORD_RETRY-success.json" to "\"Authorization\"",
			"$RECORD_RETRY-unavailable-once.json" to "\"requiredScenarioState\": \"InitialSyncUnavailable\"",
			"$RECORD_RETRY-unavailable-once.json" to "\"newScenarioState\": \"FirstFailure\"",
			"$RECORD_RETRY-success.json" to "\"requiredScenarioState\": \"FirstFailure\"",
			// Order independence of the record retry: a seeded session reads the record before the sync can
			// answer, so the first failure also starts from the initial state, and every state answers the sync.
			"$RECORD_RETRY-unavailable-once-from-start.json" to "\"equalTo\": \"Bearer record.refresh.retry.mock.access\"",
			"$RECORD_RETRY-unavailable-once-from-start.json" to "\"requiredScenarioState\": \"Started\"",
			"$RECORD_RETRY-unavailable-once-from-start.json" to "\"newScenarioState\": \"FirstFailure\"",
			"sync/post-sync-record-refresh-retry-unavailable-while-unavailable.json" to "\"requiredScenarioState\": \"InitialSyncUnavailable\"",
			"sync/post-sync-record-refresh-retry-unavailable-after-failure.json" to "\"requiredScenarioState\": \"FirstFailure\"",
			"$SYNC_RETRY-while-unavailable.json" to "\"requiredScenarioState\": \"InitialSyncUnavailable\""
		)

		val staleMappings: List<String> = listOf(
			"sync/post-sync-record-refresh-retry-success.json",
			"sync/post-sync-summary-refresh-retry-success.json",
			"summary/get-user-refresh-retry-fails-twice.json",
			"summary/get-user-refresh-retry-fails-ios-second.json"
		)
	}
}
