package com.gdavidpb.tuindice.scenarios

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The evaluations list is served by `get-evaluations-success.json` in the default state and refused or served again
 * only after a scenario puts the `evaluations-list-failure` mock in `Unavailable` or `Recovered`
 * (`evaluations-list-retry` does). The replay runs the mapping files the way WireMock does, and it fails on a tie of
 * priorities, so each state is answered by exactly one stub.
 */
class EvaluationsListMocksTest {
	private val bearer = mapOf("Authorization" to "Bearer any.session.token")

	@Test
	fun theListIsServedFromTheSuccessStubInTheDefaultState() {
		assertEquals(
			MockReplay.Reply(OK, "get-evaluations-success.json"),
			MockReplay.everything().send("GET", EVALUATIONS, bearer)
		)
	}

	@Test
	fun theListIsRefusedWith503WhileTheMockIsUnavailableAndEveryTime() {
		val replay = MockReplay.everything()

		replay.setState(LIST_FAILURE, "Unavailable")

		repeat(READS) {
			assertEquals(
				MockReplay.Reply(UNAVAILABLE, "get-evaluations-list-unavailable.json"),
				replay.send("GET", EVALUATIONS, bearer),
				"read ${it + 1}"
			)
		}
	}

	@Test
	fun theListIsServedAgainOnceTheScenarioSetsTheRecoveredState() {
		val replay = MockReplay.everything()

		replay.setState(LIST_FAILURE, "Unavailable")
		assertEquals(UNAVAILABLE, replay.send("GET", EVALUATIONS, bearer).status)

		replay.setState(LIST_FAILURE, "Recovered")

		repeat(READS) {
			assertEquals(
				MockReplay.Reply(OK, "get-evaluations-list-recovered.json"),
				replay.send("GET", EVALUATIONS, bearer),
				"read ${it + 1}"
			)
		}
	}

	@Test
	fun withoutTheRefusalStubTheUnavailableStateServesTheListAndTheReplayWouldNotice() {
		val replay = MockReplay.everything(without = "get-evaluations-list-unavailable.json")

		replay.setState(LIST_FAILURE, "Unavailable")

		assertEquals(OK, replay.send("GET", EVALUATIONS, bearer).status, "the refusal is gone, the list is served")
	}

	private companion object {
		const val EVALUATIONS = "/evaluations/v3"
		const val LIST_FAILURE = "evaluations-list-failure"
		const val READS = 3
		const val OK = 200
		const val UNAVAILABLE = 503
	}
}
