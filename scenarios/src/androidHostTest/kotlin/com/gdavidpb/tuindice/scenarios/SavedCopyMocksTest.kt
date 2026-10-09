package com.gdavidpb.tuindice.scenarios

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `enrollmentproof-saved-copy-dialog` downloads the proof of the canonical account once and then makes the service
 * answer 503: the proof is served in the default state and refused only after the scenario sets `Unavailable`.
 */
class SavedCopyMocksTest {
	private val bearer = mapOf("Authorization" to "Bearer any.session.token")
	private val body = mapOf("password" to "123456")

	@Test
	fun theServiceIsRefusedOnlyAfterTheScenarioSetsTheUnavailableState() {
		// The replay does not read the body pattern of the success stub (`$.password`), so it is left out: what is
		// checked is that the refusal does not answer in the default state, where that stub serves the proof.
		val replay = MockReplay.of(SAVED_COPY, without = "enrollment-proof-success.json")

		assertEquals(
			MockReplay.Reply(NOT_FOUND, "none"),
			replay.send("POST", "/enrollment-proof/v1", bearer, body = body),
			"in the default state the refusal does not answer"
		)

		replay.setState(SAVED_COPY, "Unavailable")

		assertEquals(
			MockReplay.Reply(UNAVAILABLE, "enrollment-proof-saved-copy-unavailable.json"),
			replay.send("POST", "/enrollment-proof/v1", bearer, body = body),
			"after the state is set the service is down"
		)
	}

	private companion object {
		const val SAVED_COPY = "enrollment-proof-saved-copy"
		const val NOT_FOUND = 404
		const val UNAVAILABLE = 503
	}
}
