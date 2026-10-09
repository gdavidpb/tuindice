package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `MockRules` on the real mappings (nothing breaks them) and on mappings made to break them, plus the structure of
 * the stubs the sign-out scenarios depend on: their states, statuses and transitions are read from the mapping
 * files, not searched for as text.
 */
class MockRulesTest {
	private val namedMappings: List<Pair<String, JsonObject>> =
		RepoFiles.allMappings.walkTopDown().filter { it.isFile && it.extension == "json" }
			.sortedBy { it.path }.map { it.name to MockJson.obj(it) }.toList()

	/** The `(WireMock scenario, state)` pairs more than one catalog scenario starts in: states every one of them sees. */
	private val sharedStartStates: Set<Pair<String, String>> =
		E2eCatalog.all.flatMap { scenario -> scenario.start.mockStates.map { it.scenario to it.state } }
			.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

	@Test
	fun everyDelayAScenarioCanSeeDeclaresWhatTheFastProfileKeeps() {
		val offenders = MockRules.delaysWithoutAFastValue(namedMappings)

		assertTrue(
			offenders.isEmpty(),
			"delays >= ${MockRules.observableDelayMs}ms need numeric metadata.fastDelayMilliseconds: $offenders"
		)
	}

	@Test
	fun aDelayWithoutAFastValueIsCaughtAtEveryLengthAScenarioCanSee() {
		fun mapping(delay: Int, metadata: String = "") =
			Json.parseToJsonElement(
				"""{"response": {"status": 200, "fixedDelayMilliseconds": $delay}$metadata}"""
			) as JsonObject

		val declared = """, "metadata": {"fastDelayMilliseconds": 250}"""
		val notANumber = """, "metadata": {"fastDelayMilliseconds": "slow"}"""
		val edge = MockRules.observableDelayMs.toInt()
		val caught = listOf(
			"three" to mapping(THREE_SECONDS),
			"edge" to mapping(edge),
			"text" to mapping(THREE_SECONDS, notANumber),
			"short" to mapping(edge - 1),
			"declared" to mapping(THREE_SECONDS, declared)
		)

		assertEquals(listOf("three", "edge", "text"), MockRules.delaysWithoutAFastValue(caught))
	}

	@Test
	fun noRefusalOnAProtectedRouteIsOnOfferToEveryScenarioInTheDefaultState() {
		val offenders = MockRules.refusalsInTheDefaultStateNotAimedAtOneAccount(
			namedMappings,
			E2eAccounts.all,
			E2eAccounts.Canonical,
			sharedStartStates
		)

		assertTrue(offenders.isEmpty(), "refusals any scenario with a shared token gets in the default state: $offenders")
	}

	@Test
	fun theStartStatesMoreThanOneScenarioSharesIncludeTheCanonicalAccountsAndNoneOfTheStatesOfOwnOnes() {
		val canonical = checkNotNull(E2eAccounts.Canonical.mockScenario) to "TokensIssued"

		assertTrue(canonical in sharedStartStates, "the canonical start is shared by many scenarios: $sharedStartStates")
		ExtraMockStatesAtStart.scenarioIds.forEach { id ->
			ExtraMockStatesAtStart.of(id).forEach {
				assertTrue(it !in sharedStartStates, "$id starts in a state of its own, $it, and is not shared")
			}
		}
	}

	@Test
	fun theFlushStubInTheDefaultStateIsCaughtAndTheOneInAStateOfItsOwnIsNot() {
		val flush = MockJson.obj(RepoFiles.file("mocks/mappings/$PENDING_FLUSH-unavailable.json"))
		val inTheDefaultState = JsonObject(flush + ("requiredScenarioState" to JsonPrimitive("Started")))
		val withoutState = JsonObject(flush - "requiredScenarioState")

		assertEquals("Unavailable", flush.string("requiredScenarioState"))
		assertEquals(
			listOf("started", "none"),
			MockRules.refusalsInTheDefaultStateNotAimedAtOneAccount(
				listOf("started" to inTheDefaultState, "own-state" to flush, "none" to withoutState),
				E2eAccounts.all,
				E2eAccounts.Canonical,
				sharedStartStates
			)
		)
	}

	@Test
	fun aRefusalAimedAtOneTokenOneAccountOrOneResourceIsNotCaughtAndTheSharedOnesAre() {
		fun refusal(request: String) =
			Json.parseToJsonElement(
				"""{"request": {"urlPath": $request}, "response": {"status": 503}}"""
			) as JsonObject

		fun password(value: String) =
			refusal(""""/record/v5/sync", "bodyPatterns": [{"matchesJsonPath": "$[?(@.password == '$value')]"}]""")

		val exactToken = """"headers": {"Authorization": {"equalTo": "Bearer pensum.retry.mock.access"}}"""
		val anyBearer = """"headers": {"Authorization": {"matches": "Bearer .+"}}"""
		val aimed = listOf(
			"token" to refusal(""""/pensums/v4", $exactToken"""),
			"password" to password("record-retry-pass"),
			"resource" to refusal(""""/subjects/v1/QB"""")
		)
		val shared = listOf(
			"canonical-password" to password(E2eAccounts.Canonical.password),
			"any-bearer" to refusal(""""/record/v5", $anyBearer"""),
			"unknown-password" to password("nobody-has-it")
		)

		assertEquals(
			emptyList(),
			MockRules.refusalsInTheDefaultStateNotAimedAtOneAccount(
				aimed,
				E2eAccounts.all,
				E2eAccounts.Canonical,
				sharedStartStates
			)
		)
		assertEquals(
			shared.map { it.first },
			MockRules.refusalsInTheDefaultStateNotAimedAtOneAccount(
				shared,
				E2eAccounts.all,
				E2eAccounts.Canonical,
				sharedStartStates
			)
		)
	}

	@Test
	fun aRefusalIsNotAimedByAPatternPathTheCanonicalTokenOrAStartStateManyScenariosShare() {
		fun refusal(request: String, state: String? = null) =
			Json.parseToJsonElement(
				"""{"request": {$request}, "response": {"status": 503}""" +
					(state?.let { """, "scenarioName": "${E2eAccounts.Canonical.mockScenario}", "requiredScenarioState": "$it"""" }
						.orEmpty()) + "}"
			) as JsonObject

		fun token(value: String) = """"headers": {"Authorization": {"equalTo": "Bearer $value"}}"""

		val canonicalToken = checkNotNull(E2eAccounts.Canonical.session).accessToken
		val ownToken = checkNotNull(E2eAccounts.PensumRetry.session).accessToken
		val shared = listOf(
			"pattern" to refusal(""""urlPathPattern": "/evaluations/v3/[A-Za-z0-9]+""""),
			"canonical-token" to refusal(""""urlPath": "/record/v5", ${token(canonicalToken)}"""),
			"unknown-token" to refusal(""""urlPath": "/record/v5", ${token("nobody.has.it")}"""),
			"shared-start" to refusal(""""urlPath": "/record/v5"""", "TokensIssued")
		)
		val aimed = listOf(
			"own-token" to refusal(""""urlPath": "/record/v5", ${token(ownToken)}"""),
			"own-token-pattern" to refusal(""""urlPathPattern": "/record/v5/.+", ${token(ownToken)}""")
		)
		val sharedStart = setOf(checkNotNull(E2eAccounts.Canonical.mockScenario) to "TokensIssued")

		fun caught(list: List<Pair<String, JsonObject>>) = MockRules.refusalsInTheDefaultStateNotAimedAtOneAccount(
			list,
			E2eAccounts.all,
			E2eAccounts.Canonical,
			sharedStart
		)

		assertEquals(shared.map { it.first }, caught(shared))
		assertEquals(emptyList(), caught(aimed))
		assertEquals(
			emptyList(),
			caught(listOf("own-state" to refusal(""""urlPath": "/record/v5", ${token(canonicalToken)}""", "Unavailable")))
		)
	}

	@Test
	fun theSignOutStubsKeepTheirStatesStatusesAndTransitions() {
		val refusal = MockJson.obj(RepoFiles.file("mocks/mappings/$PENDING_FLUSH-unavailable.json"))
		val delivery = MockJson.obj(RepoFiles.file("mocks/mappings/$PENDING_FLUSH-success.json"))
		val sibling = MockJson.obj(RepoFiles.file("mocks/mappings/evaluations/$SIBLING.json"))

		// A change the server answers 503 stays pending and is sent again: the dated add is refused in the state
		// the scenario starts the mock in, and accepted, once, from the state the scenario sets.
		assertEquals("evaluations-pending-sign-out-flush-success", refusal.string("scenarioName"))
		assertEquals("Unavailable", refusal.string("requiredScenarioState"))
		assertEquals(null, refusal.string("newScenarioState"), "the refusal never moves the mock by itself")
		assertEquals(SERVICE_UNAVAILABLE, status(refusal))
		assertEquals("evaluations-pending-sign-out-flush-success", delivery.string("scenarioName"))
		assertEquals("Available", delivery.string("requiredScenarioState"))
		assertEquals("Delivered", delivery.string("newScenarioState"))
		assertEquals(OK_STATUS, status(delivery))
		assertEquals("evaluations-pending-sign-out", sibling.string("scenarioName"))
		assertEquals("Unavailable", sibling.string("requiredScenarioState"))
		assertEquals(SERVICE_UNAVAILABLE, status(sibling))
	}

	@Test
	fun theReissueHoldsItsLoadingStateInBothProfilesLongEnoughToBeLookedAt() {
		val reissue = MockJson.obj(RepoFiles.file("mocks/mappings/login/auth-update-password-reissue-success.json"))
		val delay = ((reissue["response"] as JsonObject)["fixedDelayMilliseconds"] as JsonPrimitive).intOrNull
		val fast = ((reissue["metadata"] as JsonObject)["fastDelayMilliseconds"] as JsonPrimitive).intOrNull

		assertEquals(REISSUE_DELAY_MS, delay)
		assertEquals(REISSUE_DELAY_MS, fast)
	}

	private fun status(mapping: JsonObject): Int? =
		((mapping["response"] as JsonObject)["status"] as JsonPrimitive).intOrNull

	private companion object {
		const val PENDING_FLUSH = "evaluations/post-evaluations-pending-sign-out-flush-success"
		const val SIBLING = "post-evaluations-pending-sign-out-unavailable"
		const val THREE_SECONDS = 3000
		const val SERVICE_UNAVAILABLE = 503
		const val OK_STATUS = 200
		const val REISSUE_DELAY_MS = 5000
	}
}
