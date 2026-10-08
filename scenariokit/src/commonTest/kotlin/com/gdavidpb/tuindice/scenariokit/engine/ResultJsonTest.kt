package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome
import com.gdavidpb.tuindice.scenariokit.model.Site
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.TextEntryMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ResultJsonTest {
	private val button: Query = Query.Tag("button")
	private val field: Query = Query.Tag("field")

	private fun parse(outcome: ScenarioOutcome): JsonObject = Json.parseToJsonElement(outcome.resultJson).jsonObject

	private fun failureOf(outcome: ScenarioOutcome): JsonObject = parse(outcome).getValue("failure").jsonObject

	@Test
	fun aPassedRun_hasTheDocumentedShapeWithANullFailure() {
		val fake = FakeDriver().apply { screen[button] = FakeElement() }

		val root = parse(fake.run(Step.Tap(button)))

		assertEquals(
			listOf("scenarioId", "outcome", "startedAt", "finishedAt", "prepareBackendMs", "launchMs", "steps", "failure"),
			root.keys.toList()
		)
		assertEquals("test-scenario", root.getValue("scenarioId").jsonPrimitive.content)
		assertEquals("passed", root.getValue("outcome").jsonPrimitive.content)
		assertEquals("2026-01-01T00:00:00Z", root.getValue("startedAt").jsonPrimitive.content)
		assertEquals("2026-01-01T00:00:00Z", root.getValue("finishedAt").jsonPrimitive.content)
		assertEquals(JsonNull, root.getValue("failure"))
		val step = root.getValue("steps").jsonArray.single().jsonObject
		assertEquals(listOf("index", "primitive", "target", "durationMs", "outcome"), step.keys.toList())
		assertEquals("Tap", step.getValue("primitive").jsonPrimitive.content)
		assertEquals("tag:button", step.getValue("target").jsonPrimitive.content)
		assertEquals("passed", step.getValue("outcome").jsonPrimitive.content)
	}

	@Test
	fun theBackendResetAndTheColdStart_reportTheirOwnDuration() {
		val fake = FakeDriver().apply {
			screen[button] = FakeElement()
			launchTakesMs = 16_000
			httpTakesMs = 40
		}

		val root = parse(fake.run(Step.Tap(button)))

		// Four resets, each one request; the launch is the 16 s cold start of the app.
		assertEquals(160, root.getValue("prepareBackendMs").jsonPrimitive.int)
		assertEquals(16_000, root.getValue("launchMs").jsonPrimitive.int)
	}

	@Test
	fun aRunThatNeverLaunched_hasNoLaunchDuration() {
		val fake = FakeDriver().apply { backend.down = true }

		val root = parse(fake.run(Step.Back()))

		assertEquals(JsonNull, root.getValue("launchMs"))
		assertNotNull(root.getValue("prepareBackendMs").jsonPrimitive.content.toIntOrNull())
	}

	@Test
	fun aFailedRun_carriesTheFailureWithItsSite() {
		val site = Site("scenarios/src/commonMain/kotlin/Auth.kt", 42)
		val fake = FakeDriver()

		val outcome = fake.run(Step.WaitVisible(button, 100, site))
		val root = parse(outcome)
		val failure = failureOf(outcome)

		assertEquals("failed", root.getValue("outcome").jsonPrimitive.content)
		assertEquals(
			listOf("kind", "stepIndex", "primitive", "target", "message", "expected", "actual", "site"),
			failure.keys.toList()
		)
		assertEquals("STEP_TIMEOUT", failure.getValue("kind").jsonPrimitive.content)
		assertEquals(0, failure.getValue("stepIndex").jsonPrimitive.int)
		assertEquals("WaitVisible", failure.getValue("primitive").jsonPrimitive.content)
		assertEquals("tag:button", failure.getValue("target").jsonPrimitive.content)
		val siteJson = failure.getValue("site").jsonObject
		assertEquals("scenarios/src/commonMain/kotlin/Auth.kt", siteJson.getValue("file").jsonPrimitive.content)
		assertEquals(42, siteJson.getValue("line").jsonPrimitive.int)
		assertEquals("failed", root.getValue("steps").jsonArray.single().jsonObject.getValue("outcome").jsonPrimitive.content)
	}

	@Test
	fun everyFailureKind_comesOutInResultJson() {
		val kinds = mutableMapOf<FailureKind, ScenarioOutcome>()
		kinds[FailureKind.ASSERTION] = FakeDriver().apply {
			screen[button] = FakeElement(enabled = false)
		}.run(Step.Tap(button))
		kinds[FailureKind.STEP_TIMEOUT] = FakeDriver().run(Step.WaitVisible(button, 100))
		kinds[FailureKind.TYPED_TEXT_MISMATCH] = FakeDriver().apply {
			screen[field] = FakeElement(text = "")
			typing = { "" }
		}.run(Step.EnterText(field, "a", "ab", false, false, TextEntryMode.Keys))
		kinds[FailureKind.DRIVER_ERROR] = FakeDriver().apply { throwOn = "pressBack" }.run(Step.Back())
		kinds[FailureKind.APP_NOT_RUNNING] = FakeDriver().apply { inForeground = false }.run(Step.Tap(button))
		kinds[FailureKind.SYSTEM_DIALOG] = FakeDriver().apply { dialog = "ANR" }.run(Step.Tap(button))
		kinds[FailureKind.BACKEND_UNAVAILABLE] = FakeDriver().apply { backend.down = true }.run(Step.Back())

		assertEquals(FailureKind.entries.toSet(), kinds.keys)
		kinds.forEach { (kind, outcome) ->
			assertNotNull(outcome.failure, "$kind produced a passing run")
			assertEquals(kind.name, failureOf(outcome).getValue("kind").jsonPrimitive.content, outcome.report)
		}
	}

	@Test
	fun theTypedTextFailure_carriesExpectedAndActual() {
		val fake = FakeDriver().apply {
			screen[field] = FakeElement(text = "")
			typing = { it.replace("2", "") }
		}

		val failure = failureOf(fake.run(Step.EnterText(field, "1234567", "12-34567", false, false, TextEntryMode.Keys)))

		assertEquals("12-34567", failure.getValue("expected").jsonPrimitive.content)
		assertEquals("134567", failure.getValue("actual").jsonPrimitive.content)
	}
}
