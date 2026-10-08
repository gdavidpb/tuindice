package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.HttpReply
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlinx.serialization.json.JsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The journal checks against the real WireMock: the order `POST /__admin/requests/find` answers in, the order of
 * `GET /__admin/requests`, and the status that the latter carries for each response. The fake of the other tests
 * copies what these prove.
 */
class WireMockJournalOrderTest {
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

	private fun expect(basicAuth: String?, status: Int? = null) =
		Step.ExpectRequest("POST", route, basicAuth, TIMEOUT_MS, status)

	private fun bodies(reply: HttpReply, key: (JsonObject) -> String?) =
		WireMockJson.array(WireMockJson.obj(reply.body), "requests").map(key)

	@Test
	fun findAnswersFromTheOldestRequestToTheMostRecent() {
		wiremock.stubPost(route, SERVICE_UNAVAILABLE, priority = LOW)
		appPosts("11-11111:first", "one")
		appPosts("11-11111:second", "two")

		val reply = wiremock.http("POST", "/__admin/requests/find", """{"method":"POST","urlPath":"$route"}""", null)
		val found = bodies(reply) { WireMockJson.text(it, "body") }

		assertEquals(listOf("one", "two"), found)
	}

	@Test
	fun theJournalListingAnswersFromTheMostRecentRequestToTheOldest() {
		wiremock.stubPost(route, SERVICE_UNAVAILABLE, priority = LOW)
		appPosts("11-11111:first", "one")
		appPosts("11-11111:second", "two")

		val reply = wiremock.http("GET", "/__admin/requests", null, null)
		val listed = bodies(reply) { WireMockJson.text(WireMockJson.child(it, "request"), "body") }

		assertEquals(listOf("two", "one"), listed)
	}

	@Test
	fun expectRequestJudgesTheMostRecentRequestOfTheRouteAgainstTheRealJournal() {
		wiremock.stubPost(route, SERVICE_UNAVAILABLE, priority = LOW)
		appPosts("11-11111:outdated", "old")
		appPosts("11-11111:12456", "new")

		val failure = engine.expectRequest(expect("11-11111:123456")) as StepResult.Failed

		assertEquals(FailureKind.TYPED_TEXT_MISMATCH, failure.kind, failure.message)
		assertEquals("11-11111:12456", failure.actual)
	}

	@Test
	fun expectRequestListsTheCredentialsMostRecentFirstAgainstTheRealJournal() {
		wiremock.stubPost(route, SERVICE_UNAVAILABLE, priority = LOW)
		appPosts("11-11111:outdated", "old")
		appPosts("22-22222:123456", "new")

		val failure = engine.expectRequest(expect("11-11111:123456")) as StepResult.Failed

		assertEquals(FailureKind.STEP_TIMEOUT, failure.kind, failure.message)
		assertContains(failure.message, "\"22-22222:123456\", \"11-11111:outdated\"")
	}

	@Test
	fun expectRequestWithAStatusPassesOnlyWhenARequestWasAnsweredWithIt() {
		wiremock.stubPost(route, SERVICE_UNAVAILABLE, priority = LOW)
		wiremock.stubPost(route, OK, priority = HIGH, bodyContains = "accepted")
		appPosts("11-11111:123456", "refused")

		val refused = engine.expectRequest(expect("11-11111:123456", status = OK))
		assertTrue(refused is StepResult.Failed, "a 503 is not a 200")

		appPosts("11-11111:123456", "accepted")

		assertEquals(StepResult.Passed, engine.expectRequest(expect("11-11111:123456", status = OK)))
		assertEquals(StepResult.Passed, engine.expectRequest(expect(null, status = SERVICE_UNAVAILABLE)))
		assertTrue(engine.expectRequest(expect("22-22222:123456", status = OK)) is StepResult.Failed)
	}

	private companion object {
		const val OK = 200
		const val SERVICE_UNAVAILABLE = 503
		const val LOW = 5
		const val HIGH = 1
		const val TIMEOUT_MS = 400L
	}
}
