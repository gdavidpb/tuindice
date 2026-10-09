package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

	/**
	 * YE-7: the first download is held by the mock for its slow profile (the fast profile of the harness keeps it longer
	 * still: 15 s against 10 s), and the wait for the app to hand the file over starts when the proof is asked for. Its
	 * window has to hold that delay and leave at least [MARGIN_MS] to decode the PDF, open the viewer and let the driver
	 * see the app leave; a window that is only the delay is a false red on a loaded device.
	 */
	@Test
	fun theWaitForTheFirstDownloadToLeaveTheAppHoldsTheDelayOfTheMockWithRoomToSpare() {
		val mapping = MockJson.obj(RepoFiles.file("mocks/mappings/enrollmentproof/enrollment-proof-success.json"))
		val fastDelay = mapping.string("metadata", "fastDelayMilliseconds")!!.toLong()
		val slowDelay = mapping.string("response", "fixedDelayMilliseconds")!!.toLong()

		SCENARIOS.forEach { id ->
			val steps = E2eCatalog.all.single { it.id == id }.steps
			val asked = steps.indexOfFirst { it is Step.Group && it.name == "openCurrentEnrollmentProof" }
			val wait = steps[asked + 1] as Step.WaitBackgrounded

			assertTrue(
				wait.timeoutMs >= maxOf(fastDelay, slowDelay) + MARGIN_MS,
				"$id waits ${wait.timeoutMs} ms for the app to leave after a download the mock holds for $fastDelay ms"
			)
		}
	}

	private companion object {
		const val MARGIN_MS = 10_000L
		val SCENARIOS = listOf("enrollmentproof-saved-copy-dialog", "enrollmentproof-saved-copy-gone-after-sign-out")
		const val SAVED_COPY = "enrollment-proof-saved-copy"
		const val NOT_FOUND = 404
		const val UNAVAILABLE = 503
	}
}
