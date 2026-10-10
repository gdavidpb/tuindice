package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A change the server answers 503 stays pending and is sent again, so the two sign-out scenarios that need that
 * answer start their WireMock scenarios in a state of their own in which the dated or undated add is refused. The
 * refusal is not the state every scenario starts in: any other scenario that adds an evaluation as the same
 * account gets it accepted. The replay runs the mapping files the way WireMock does, with the states the catalog
 * hands each scenario at its start; the negative cases remove the refusing stub, or the state, and expect the
 * replay to notice.
 */
class PendingFlushMocksTest {
	private val canonicalBearer =
		mapOf("Authorization" to "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.exchange.mock.access")
	private val datedAdd = mapOf("type" to "0", "date" to "2026-08-15")
	private val undatedAdd = mapOf("type" to "0", "date" to null)

	@Test
	fun theAddOfAScenarioThatIsNotASignOutOneIsAcceptedWhateverItsDate() {
		val replay = startedAs("evaluations-add-submit")

		assertEquals(OK, replay.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd).status, "a dated add")
		assertEquals(OK, replay.send("POST", EVALUATIONS, canonicalBearer, body = undatedAdd).status, "an undated add")
	}

	@Test
	fun theDatedAddIsRefusedWith503EveryTimeUntilTheScenarioSetsTheMockAvailableAndThenSent() {
		val replay = startedAs("auth-pending-sign-out-flush-success")

		repeat(RESENDS_WHILE_UNAVAILABLE) {
			val sent = replay.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd)

			assertEquals(UNAVAILABLE, sent.status, "send ${it + 1}")
		}

		replay.setState(PENDING_FLUSH, "Available")

		assertEquals(
			MockReplay.Reply(OK, "post-evaluations-pending-sign-out-flush-success-success.json"),
			replay.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd)
		)
		assertEquals(OK, replay.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd).status, "once delivered")
	}

	@Test
	fun theUndatedAddOfTheOtherSignOutScenarioIsRefusedUntilItsOwnStateChanges() {
		val replay = startedAs("auth-pending-sign-out")

		repeat(RESENDS_WHILE_UNAVAILABLE) {
			assertEquals(
				MockReplay.Reply(UNAVAILABLE, "post-evaluations-pending-sign-out-unavailable.json"),
				replay.send("POST", EVALUATIONS, canonicalBearer, body = undatedAdd),
				"send ${it + 1}"
			)
		}
		assertEquals(OK, replay.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd).status, "a dated add")
	}

	@Test
	fun theTwoSignOutScenariosDoNotRefuseEachOthersAdd() {
		val flush = startedAs("auth-pending-sign-out-flush-success")
		val plain = startedAs("auth-pending-sign-out")

		assertEquals(OK, flush.send("POST", EVALUATIONS, canonicalBearer, body = undatedAdd).status)
		assertEquals(OK, plain.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd).status)
	}

	@Test
	fun withoutTheStateTheScenarioStartsInTheRefusalDoesNotHappen() {
		val withoutTheStart = MockReplay.everything().also { it.setState(LOGIN_LIFECYCLE, "TokensIssued") }

		assertEquals(OK, withoutTheStart.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd).status)
		assertEquals(OK, withoutTheStart.send("POST", EVALUATIONS, canonicalBearer, body = undatedAdd).status)
	}

	@Test
	fun withoutTheStubThatRefusesTheDatedAddTheReplayGetsItAcceptedAtOnce() {
		val broken = startedAs(
			"auth-pending-sign-out-flush-success",
			without = "post-evaluations-pending-sign-out-flush-success-unavailable.json"
		)

		assertEquals(OK, broken.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd).status)
	}

	/** The mapping files with the WireMock states the catalog hands [scenarioId] at its start. */
	private fun startedAs(scenarioId: String, without: String? = null): MockReplay {
		val replay = MockReplay.everything(without)

		E2eCatalog.all.single { it.id == scenarioId }.start.mockStates.forEach { replay.setState(it.scenario, it.state) }

		return replay
	}

	private companion object {
		const val PENDING_FLUSH = "evaluations-pending-sign-out-flush-success"
		const val LOGIN_LIFECYCLE = "login-token-lifecycle"
		const val EVALUATIONS = "/evaluations/v3"
		const val RESENDS_WHILE_UNAVAILABLE = 3
		const val OK = 200
		const val UNAVAILABLE = 503
	}
}
