package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * The rules about how the mappings under `mocks/` are written, as functions over mapping objects that return what
 * breaks them, so `MockContractTest` runs each on the real mappings (nothing breaks it) and on a mapping made to
 * break it. A mapping comes with the name of its file, which is how the rules name an offender.
 */
internal object MockRules {
	private val protectedPath = Regex(
		"""^/(users/v1($|/)|messaging/v1$|record/v5($|/)|evaluations/v3($|/)""" +
			"""|enrollment-proof/v1$|subjects/v1($|/)|pensums/v4$)"""
	)
	private val resourcePath = Regex("""^/subjects/v1/[^/]+$""")
	private val pinnedPassword = Regex("""@\.password == '([^']*)'""")

	/**
	 * The shortest delay a step can look at, which is the window of a quick `ifVisible`: a shorter one is over before
	 * anything on the screen can be observed, so collapsing it to the fast default cannot change what a scenario sees.
	 */
	val observableDelayMs: Double = Timeouts.Probe.toDouble()

	/**
	 * Names of the mappings whose delay a scenario can observe (at least [observableDelayMs]) and that do not say
	 * what the fast profile keeps. Without `metadata.fastDelayMilliseconds` the fast profile collapses the delay to
	 * its default in silence (`apply-fast-delay-profile.sh`), which is how a scenario that waits on the delay goes
	 * red on a mock nobody changed. A mapping that does not mind the collapse says so with the fast default. A
	 * marker that is not a number is wrong too.
	 */
	fun delaysWithoutAFastValue(mappings: List<Pair<String, JsonObject>>): List<String> =
		mappings.filter { (_, mapping) ->
			val delay = number((mapping["response"] as? JsonObject)?.get("fixedDelayMilliseconds"))
			val fast = (mapping["metadata"] as? JsonObject)?.get("fastDelayMilliseconds")

			(delay != null && delay >= observableDelayMs && fast == null) || (fast != null && number(fast) == null)
		}.map { it.first }

	/**
	 * Names of the mappings that refuse (status 400 or more) on a protected route in the state every WireMock
	 * scenario starts in, or in no state, and are not aimed at one account. Such a stub answers every scenario that
	 * sends the request with a token it matches, and only the scenario that needed the refusal should get it. It is
	 * aimed when it matches one exact token, pins in the body the password of an account other than
	 * [defaultAccount], or names one resource in the path. Otherwise it belongs in a state of its own, which the
	 * scenario that needs it puts the mock in at its start (`Start.Seeded(mockStates = ...)`).
	 */
	fun refusalsInTheDefaultStateNotAimedAtOneAccount(
		mappings: List<Pair<String, JsonObject>>,
		accounts: List<E2eAccount>,
		defaultAccount: E2eAccount
	): List<String> =
		mappings.filter { (_, mapping) ->
			val request = mapping["request"] as? JsonObject
			val path = request?.string("urlPath").orEmpty()
			val status = number((mapping["response"] as? JsonObject)?.get("status")) ?: 0.0
			val state = mapping.string("requiredScenarioState")
			val password = request?.array("bodyPatterns").orEmpty()
				.firstNotNullOfOrNull { pinnedPassword.find((it as? JsonObject)?.string("matchesJsonPath").orEmpty()) }
				?.groupValues?.get(1)
			val aimed = request?.string("headers", "Authorization", "equalTo") != null ||
				(password != null && password != defaultAccount.password && accounts.any { it.password == password }) ||
				resourcePath.matches(path)

			status >= HTTP_ERROR && (state == null || state == STARTED) && protectedPath.containsMatchIn(path) && !aimed
		}.map { it.first }

	private fun number(element: JsonElement?): Double? = (element as? JsonPrimitive)?.doubleOrNull

	private const val HTTP_ERROR = 400.0
	private const val STARTED = "Started"
}
