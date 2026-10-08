package com.gdavidpb.tuindice.scenarios

/**
 * The scenarios that start with WireMock states besides their account's `TokensIssued`, and which: a mock that
 * refuses what only they send is not in the state every scenario starts in (`MockRules` forbids that), so they
 * put it in its own state from the start. Adding a scenario here is a decision, not a convenience.
 */
internal object ExtraMockStatesAtStart {
	private val byScenario: Map<String, List<Pair<String, String>>> = mapOf(
		"auth-pending-sign-out" to listOf("evaluations-pending-sign-out" to "Unavailable"),
		"auth-pending-sign-out-flush-success" to listOf("evaluations-pending-sign-out-flush-success" to "Unavailable")
	)

	val scenarioIds: Set<String> = byScenario.keys

	/** The `(WireMock scenario, state)` pairs [scenarioId] starts with besides its account's. */
	fun of(scenarioId: String): List<Pair<String, String>> = byScenario[scenarioId].orEmpty()
}
