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
		const val UNAVAILABLE_BODY = "\"bodyFileName\": \"sync/post-sync-record-unavailable.json\""

		private const val SUMMARY_USER = "summary/get-user-refresh-retry"
		private const val RECORD_RETRY = "record/get-record-refresh-retry"

		/** Mapping (relative to `mocks/mappings`) and a matcher it has to keep. */
		val expectations: List<Pair<String, String>> = listOf(
			"record/patch-synthetic-term-rejected.json" to "\"equalTo\": \"Bearer record.term.rejected.mock.access\"",
			"login/auth-record-term-rejected-exchange-success.json" to "\"access_token\": \"record.term.rejected.mock.access\"",
			"sync/post-sync-summary-refresh-retry-unavailable.json" to "\$[?(@.password == 'summary-retry-pass')]",
			"sync/post-sync-summary-refresh-retry-unavailable.json" to UNAVAILABLE_BODY,
			"sync/post-sync-summary-refresh-retry-unavailable.json" to "\"newScenarioState\": \"InitialSyncUnavailable\"",
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
			"$RECORD_RETRY-success.json" to "\"requiredScenarioState\": \"FirstFailure\""
		)

		val staleMappings: List<String> = listOf(
			"sync/post-sync-record-refresh-retry-success.json",
			"sync/post-sync-summary-refresh-retry-success.json",
			"summary/get-user-refresh-retry-fails-twice.json",
			"summary/get-user-refresh-retry-fails-ios-second.json"
		)
	}
}
