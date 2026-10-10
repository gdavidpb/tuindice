package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExpectRequestTest {
	private val go: Query = Query.Tag("go")
	private val bootstrap = "/auth/v2/bootstrap"
	private val users = "/users/v1"

	private fun expect(basicAuth: String? = "11-11111:123456", timeoutMs: Long = 1_000) =
		Step.ExpectRequest("POST", bootstrap, basicAuth, timeoutMs)

	private fun expectAnswered(status: Int, atLeast: Int = 1) =
		Step.ExpectRequest("GET", users, null, 1_000, status, atLeast)

	private fun FakeDriver.answers(vararg statuses: Int) =
		statuses.forEach { backend.appRequest("GET", users, it, null) }

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

	@Test
	fun expectRequest_withAStatusCountsTheRequestsOfTheJournalFromTheStartOfTheScenario() {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = { fake.answers(SERVICE_UNAVAILABLE) }

		assertPassed(fake.run(Step.Tap(go), expectAnswered(SERVICE_UNAVAILABLE)))
	}

	@Test
	fun expectRequest_withAtLeastTwo_failsWithOnlyTheRequestOfTheStartAndPassesOnTheSecond() {
		val early = FakeDriver()
		early.screen[go] = FakeElement()
		early.onTap[go] = { early.answers(SERVICE_UNAVAILABLE) }
		assertFailed(early.run(Step.Tap(go), expectAnswered(SERVICE_UNAVAILABLE, atLeast = 2)), FailureKind.STEP_TIMEOUT)

		val second = FakeDriver()
		second.screen[go] = FakeElement()
		second.onTap[go] = { second.answers(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE) }
		assertPassed(second.run(Step.Tap(go), expectAnswered(SERVICE_UNAVAILABLE, atLeast = 2)))
	}

	@Test
	fun expectRequest_withoutAStatusAndAtLeastTwo_alsoNeedsTheSecondRequest() {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = { fake.backend.appRequest("POST", bootstrap, 401, "Basic anything") }
		val step = Step.ExpectRequest("POST", bootstrap, null, 1_000, atLeast = 2)

		assertFailed(fake.run(Step.Tap(go), step), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun expectRequest_withAStatusThatExpires_listsWhatTheRouteAnsweredMostRecentFirst() {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = { fake.answers(SERVICE_UNAVAILABLE, TOO_MANY, SERVICE_UNAVAILABLE) }

		val failure = assertFailed(fake.run(Step.Tap(go), expectAnswered(OK)), FailureKind.STEP_TIMEOUT)

		assertContains(failure.message, "GET /users/v1 was not answered with 200")
		assertContains(failure.message, "it answered, most recent first: 503, 429, 503")
		assertEquals("GET /users/v1 answered 200", failure.target)
	}

	@Test
	fun expectRequest_needsAtLeastOneRequest_soItCannotPassWithoutAny() {
		for (count in listOf(0, -1, Int.MIN_VALUE)) {
			val failure = assertFailsWith<IllegalArgumentException> { expectAnswered(SERVICE_UNAVAILABLE, atLeast = count) }

			assertContains(failure.message.orEmpty(), "at least one request")
		}
		assertEquals(1, expectAnswered(SERVICE_UNAVAILABLE, atLeast = 1).atLeast)
	}

	@Test
	fun expectRequest_target_namesTheStatusAndAtLeastOnlyWhenTheyAreNotTheDefault() {
		assertEquals("POST /auth/v2/bootstrap", expect().target)
		assertEquals("GET /users/v1 answered 503", expectAnswered(SERVICE_UNAVAILABLE).target)
		assertEquals("GET /users/v1 answered 503, at least 2", expectAnswered(SERVICE_UNAVAILABLE, atLeast = 2).target)
	}

	@Test
	fun theFakeJournal_answersEveryRequestWithoutALimitAndOnlyTheMostRecentWithOne() {
		val fake = FakeDriver()
		fake.answers(1, 2, 3, 4, 5, 6, 7, 8)

		fun statuses(path: String): List<Int?> {
			val body = fake.backend.http("GET", path, null, null).body
			return WireMockJson.array(WireMockJson.obj(body), "requests")
				.map { WireMockJson.number(WireMockJson.child(it, "response"), "status") }
		}

		assertEquals(listOf(8, 7, 6, 5, 4, 3, 2, 1), statuses("/__admin/requests"))
		assertEquals(listOf(8, 7, 6), statuses("/__admin/requests?limit=3"))
	}

	private companion object {
		const val OK = 200
		const val TOO_MANY = 429
		const val SERVICE_UNAVAILABLE = 503
	}
}
