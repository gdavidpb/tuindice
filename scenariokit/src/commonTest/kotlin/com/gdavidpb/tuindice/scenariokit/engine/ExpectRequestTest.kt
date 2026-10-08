package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ExpectRequestTest {
	private val go: Query = Query.Tag("go")
	private val bootstrap = "/auth/v2/bootstrap"

	private fun expect(basicAuth: String? = "11-11111:123456", timeoutMs: Long = 1_000) =
		Step.ExpectRequest("POST", bootstrap, basicAuth, timeoutMs)

	/** The backend journal is reset at the start of a run, so the app sends its request when [go] is tapped. */
	private fun runAfterTapSends(request: AppRequest?, step: Step): ScenarioOutcome {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = { request?.let { fake.backend.appRequest(it.method, it.url, it.status, it.authorization) } }
		return fake.run(Step.Tap(go), step)
	}

	@Test
	fun expectRequest_passesWhenTheRequestArrivedWithTheCredential() {
		val sent = AppRequest("POST", bootstrap, 200, BasicAuth.header("11-11111:123456"))

		assertPassed(runAfterTapSends(sent, expect()))
	}

	@Test
	fun expectRequest_withoutCredential_matchesByMethodAndPath() {
		val sent = AppRequest("POST", bootstrap, 401, "Basic anything")

		assertPassed(runAfterTapSends(sent, expect(basicAuth = null)))
	}

	@Test
	fun expectRequest_whenNoRequestArrives_timesOut() {
		val failure = assertFailed(runAfterTapSends(null, expect()), FailureKind.STEP_TIMEOUT)

		assertContains(failure.message, "no POST /auth/v2/bootstrap request reached the backend")
	}

	@Test
	fun expectRequest_whenTheBackendReceivedAnotherCredential_isATypedTextMismatch() {
		val sent = AppRequest("POST", bootstrap, 401, BasicAuth.header("11-11111:12456"))

		val failure = assertFailed(runAfterTapSends(sent, expect()), FailureKind.TYPED_TEXT_MISMATCH)

		assertEquals("11-11111:123456", failure.expected)
		assertEquals("11-11111:12456", failure.actual)
		assertContains(failure.message, "typed \"11-11111:123456\" but the backend received \"11-11111:12456\"")
	}

	@Test
	fun expectRequest_whenTheRequestHasNoBasicCredential_isATypedTextMismatch() {
		val sent = AppRequest("POST", bootstrap, 401, "Bearer token")

		val failure = assertFailed(runAfterTapSends(sent, expect()), FailureKind.TYPED_TEXT_MISMATCH)

		assertContains(failure.message, "no Basic credential")
	}

	@Test
	fun expectRequest_whenTheLatestHasAnotherIdentifierAndOlderOnesAreTheOldPassword_isATimeoutListingWhatItSaw() {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = {
			fake.backend.appRequest("POST", bootstrap, 401, BasicAuth.header("11-11111:outdated"))
			fake.backend.appRequest("POST", bootstrap, 401, BasicAuth.header("22-22222:123456"))
		}

		val failure = assertFailed(fake.run(Step.Tap(go), expect()), FailureKind.STEP_TIMEOUT)

		assertContains(failure.message, "did not reach the backend as expected")
		assertContains(failure.message, "\"22-22222:123456\", \"11-11111:outdated\"")
	}

	@Test
	fun expectRequest_whenTheLatestKeepsTheIdentifierWithAnotherPasswordAfterOlderOnes_isATypedTextMismatch() {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = {
			fake.backend.appRequest("POST", bootstrap, 401, BasicAuth.header("11-11111:outdated"))
			fake.backend.appRequest("POST", bootstrap, 401, BasicAuth.header("11-11111:12456"))
		}

		val failure = assertFailed(fake.run(Step.Tap(go), expect()), FailureKind.TYPED_TEXT_MISMATCH)

		assertEquals("11-11111:12456", failure.actual)
	}

	@Test
	fun expectRequest_whenAnOldRequestWithAnotherCredentialIsNotTheOnlyOne_theStepDoesNotBlameTheTyping() {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = {
			fake.backend.appRequest("POST", bootstrap, 401, BasicAuth.header("11-11111:outdated"))
			fake.backend.appRequest("POST", bootstrap, 401, "Bearer token")
		}

		val failure = assertFailed(fake.run(Step.Tap(go), expect()), FailureKind.STEP_TIMEOUT)

		assertContains(failure.message, "\"no Basic credential\", \"11-11111:outdated\"")
	}

	@Test
	fun expectRequest_whenWireMockGoesDownMidScenario_isBackendUnavailable() {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = { fake.backend.down = true }

		val outcome = fake.run(Step.Tap(go), expect())

		assertFailed(outcome, FailureKind.BACKEND_UNAVAILABLE, stepIndex = 1)
	}
}
