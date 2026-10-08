package com.gdavidpb.tuindice.scenarios

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A change the server answers 503 stays pending and is sent again, so the mock of `auth-pending-sign-out-flush-success`
 * refuses the dated add every time until the scenario sets its state to Available. The replay runs the mapping files
 * the way WireMock does; the negative case removes the refusing stub and expects the replay to notice.
 */
class PendingFlushMocksTest {
	private val canonicalBearer =
		mapOf("Authorization" to "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.exchange.mock.access")
	private val datedAdd = mapOf("type" to "0", "date" to "2026-08-15")

	@Test
	fun theDatedAddIsRefusedWithA503EveryTimeUntilTheScenarioSetsTheMockAvailableAndThenSent() {
		val replay = MockReplay.of(PENDING_FLUSH, LOGIN_LIFECYCLE)

		replay.setState(LOGIN_LIFECYCLE, "TokensIssued")
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
	fun theUndatedAddOfTheOtherPendingSignOutStaysRefusedAndDoesNotTieWithTheDatedOne() {
		val replay = MockReplay.of(PENDING_FLUSH, LOGIN_LIFECYCLE)

		replay.setState(LOGIN_LIFECYCLE, "TokensIssued")
		replay.setState(PENDING_FLUSH, "Available")

		assertEquals(
			MockReplay.Reply(UNAVAILABLE, "post-evaluations-pending-sign-out-unavailable.json"),
			replay.send("POST", EVALUATIONS, canonicalBearer, body = mapOf("type" to "0", "date" to null))
		)
	}

	@Test
	fun withoutTheStubThatRefusesTheDatedAddTheReplayGetsItAcceptedAtOnce() {
		val broken = MockReplay.of(
			PENDING_FLUSH,
			LOGIN_LIFECYCLE,
			without = "post-evaluations-pending-sign-out-flush-success-unavailable.json"
		)

		broken.setState(LOGIN_LIFECYCLE, "TokensIssued")

		assertEquals(OK, broken.send("POST", EVALUATIONS, canonicalBearer, body = datedAdd).status)
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
