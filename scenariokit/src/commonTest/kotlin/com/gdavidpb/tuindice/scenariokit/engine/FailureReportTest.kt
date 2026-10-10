package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Site
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class FailureReportTest {
	private val go: Query = Query.Tag("go")
	private val bootstrap = "/auth/v2/bootstrap"
	private val start = LaunchSpec(emptyMap(), listOf(MockState("login-token-lifecycle", "TokensIssued")))

	private fun failingRun(requests: List<AppRequest>): String {
		val fake = FakeDriver()
		fake.screen[go] = FakeElement()
		fake.onTap[go] = { requests.forEach { fake.backend.appRequest(it.method, it.url, it.status, it.authorization) } }
		val site = Site("scenarios/src/commonMain/kotlin/Auth.kt", 17)
		return fake.run(
			scenarioOf(Step.Tap(go), Step.WaitVisible(Query.Tag("never"), 100, site), start = start)
		).report
	}

	@Test
	fun report_carriesTheDecodedBasicCredentialOfTheLastBootstrap() {
		val report = failingRun(listOf(AppRequest("POST", bootstrap, 401, BasicAuth.header("11-11111:12456"))))

		assertContains(report, "POST $bootstrap -> 401 [Basic credential: 11-11111:12456]")
	}

	@Test
	fun report_listsMockScenariosThatAreNotInStarted() {
		val report = failingRun(emptyList())

		assertContains(report, "login-token-lifecycle = TokensIssued")
		assertFalse("other = " in report, report)
	}

	@Test
	fun report_namesTheStepTheSiteAndTheKind() {
		val report = failingRun(emptyList())

		assertContains(report, "STEP_TIMEOUT at step 1 (WaitVisible tag:never)")
		assertContains(report, "at scenarios/src/commonMain/kotlin/Auth.kt:17")
	}

	@Test
	fun report_keepsOnlyTheLastFiveRequestsNewestFirst() {
		val report = failingRun((1..7).map { AppRequest("GET", "/r$it", 200, null) })

		assertContains(report, "GET /r7 -> 200")
		assertContains(report, "GET /r3 -> 200")
		assertFalse("GET /r2 -> 200" in report, report)
		assertFalse("GET /r1 -> 200" in report, report)
		assertContains(report.substringAfter("newest first"), "GET /r7")
	}
}
