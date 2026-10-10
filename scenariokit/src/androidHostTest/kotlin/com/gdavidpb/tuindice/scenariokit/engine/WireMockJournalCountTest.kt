package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** How many requests `ExpectRequest` needs, against the real WireMock journal (which is not emptied between steps). */
class WireMockJournalCountTest {
	private lateinit var wiremock: RealWireMock
	private val route = "/auth/v1/token"

	@BeforeTest
	fun startWireMock() {
		wiremock = RealWireMock()
	}

	@AfterTest
	fun stopWireMock() {
		wiremock.close()
	}

	private fun appPosts(credential: String, body: String) = wiremock.appPosts(route, credential, body)

	private val engine: BackendEngine
		get() = FakeDriver().let { fake -> BackendEngine(wiremock, Poller(fake, fake.time)) }

	private fun expect(atLeast: Int) = Step.ExpectRequest("POST", route, null, TIMEOUT_MS, SERVICE_UNAVAILABLE, atLeast)

	@Test
	fun expectRequestWithAtLeastTwoCountsTheJournalFromTheStartOfTheScenarioAgainstTheRealJournal() {
		wiremock.stubPost(route, SERVICE_UNAVAILABLE, priority = LOW)
		appPosts("11-11111:123456", "launch")

		assertEquals(StepResult.Passed, engine.expectRequest(expect(atLeast = 1)))
		val early = engine.expectRequest(expect(atLeast = 2))
		val failure = assertIs<StepResult.Failed>(early, "the request before the step is the only one: a second is needed")
		assertContains(failure.message, "it answered, most recent first: 503")

		appPosts("11-11111:123456", "retry")

		assertEquals(StepResult.Passed, engine.expectRequest(expect(atLeast = 2)))
	}

	private companion object {
		const val SERVICE_UNAVAILABLE = 503
		const val LOW = 5
		const val TIMEOUT_MS = 400L
	}
}
