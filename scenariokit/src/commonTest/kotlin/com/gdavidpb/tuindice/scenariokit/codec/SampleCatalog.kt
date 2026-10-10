package com.gdavidpb.tuindice.scenariokit.codec

import com.gdavidpb.tuindice.scenariokit.contract.DriverContractFixture
import com.gdavidpb.tuindice.scenariokit.model.CatalogAccount
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Quarantine
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.ScenarioCatalog
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenariokit.model.Site
import com.gdavidpb.tuindice.scenariokit.model.Step

val SAMPLE_SITE = Site("scenarios/src/commonMain/kotlin/Sample.kt", 10)

private val tag = Query.Tag("button")

/** Every step kind once, nested where the kind can nest. */
fun allStepKinds(site: Site? = SAMPLE_SITE): List<Step> = listOf(
	Step.Relaunch(mapOf("B" to "2", "A" to "1"), site),
	Step.Foreground(site),
	Step.Tap(tag, true, site),
	Step.TapAt(tag, 0.5, 0.25, site),
	Step.DoubleTap(Query.Text("Save", contains = true), site),
	Step.Back(site),
	Step.EnterText(tag, "12-34567", "12-34567", false, true, site),
	Step.SubmitTextEntry(site),
	Step.WaitVisible(tag, 5_000, site),
	Step.WaitGone(Query.System("Cancel"), 5_000, site),
	Step.WaitBackgrounded(5_000, site),
	Step.AssertChecked(tag, true, 5_000, site),
	Step.AssertEnabled(tag, false, 5_000, site),
	Step.Swipe(null, 0.5, 0.8, 0.0, -0.4, 400, site),
	Step.ScrollUntilVisible(tag, Scroll.ContentDown, 20_000, site),
	Step.IfVisible(tag, 1_500, listOf(Step.Tap(tag, true, site)), site),
	Step.OnPlatform(Platform.Ios, listOf(Step.Back(site)), site),
	Step.SetMockState("login-token-lifecycle", "Reissued", site),
	Step.ExpectRequest("POST", "/auth/v2/bootstrap", "11-11111:123456", 20_000, site = site),
	Step.Group("sign in", listOf(Step.Tap(tag, true, site)), site)
)

fun sampleScenario(id: String = "auth-login-success", steps: List<Step> = allStepKinds()) = Scenario(
	id = id,
	module = "auth",
	start = LaunchSpec(
		mapOf("TUINDICE_E2E_NETWORK_AVAILABLE" to "true"),
		listOf(MockState("login-token-lifecycle", "TokensIssued"))
	),
	steps = steps,
	covers = listOf("SignIn.Submit"),
	tags = listOf("smoke"),
	platforms = listOf(Platform.Android, Platform.Ios),
	account = "canonical",
	signsIn = true,
	timeoutSeconds = 120,
	quarantine = Quarantine("flaky on CI", "2026-12-31")
)

fun sampleCatalog(scenarios: List<Scenario> = listOf(sampleScenario())) = ScenarioCatalog(
	accounts = listOf(
		CatalogAccount("canonical", "11-11111", "123456", "sid", "access", "refresh", "login-token-lifecycle", "11-11111"),
		CatalogAccount("invalid", "00-00000", "bad-password", null, null, null, null)
	),
	scenarios = scenarios,
	contractFixture = DriverContractFixture(
		start = LaunchSpec(emptyMap()),
		presentTag = "present",
		absentTag = "absent",
		disabledTag = "disabled",
		textFieldTag = "field",
		secureFieldTag = "secret",
		secureSample = "abcdefghijklmnopqrstuvwxy",
		textSample = "ab1",
		expectedText = "ab1"
	)
)
