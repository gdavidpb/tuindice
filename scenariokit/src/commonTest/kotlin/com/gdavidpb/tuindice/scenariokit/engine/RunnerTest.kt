package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.codec.sampleCatalog
import com.gdavidpb.tuindice.scenariokit.codec.sampleScenario
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunnerTest {
	private val button: Query = Query.Tag("button")
	private val catalog = CatalogCodec.encode(
		sampleCatalog(
			listOf(
				sampleScenario("a-pass", steps = listOf(Step.WaitGone(button, 100))),
				sampleScenario("a-fail", steps = listOf(Step.WaitVisible(button, 100)))
			)
		)
	)

	@Test
	fun run_returnsThePassedOutcomeOfTheNamedScenario() {
		val fake = FakeDriver()

		val outcome = ScenarioRunner.runWith(catalog, "a-pass", fake, fake.clocks)

		assertPassed(outcome)
		assertEquals("a-pass", outcome.scenarioId)
	}

	@Test
	fun run_withTheDefaultClocks_alsoWorks() {
		val outcome = ScenarioRunner.run(catalog, "a-pass", FakeDriver())

		assertPassed(outcome)
		assertEquals(
			"passed",
			Json.parseToJsonElement(outcome.resultJson).jsonObject.getValue("outcome").jsonPrimitive.content
		)
	}

	@Test
	fun run_returnsAFailedOutcomeForAFailingScenario() {
		val fake = FakeDriver()

		assertFailed(ScenarioRunner.runWith(catalog, "a-fail", fake, fake.clocks), FailureKind.STEP_TIMEOUT, stepIndex = 0)
	}

	@Test
	fun run_whenTheDriverThrowsOnLaunch_returnsACrashedOutcomeInsteadOfThrowing() {
		val fake = FakeDriver().apply { throwOn = "launch" }

		val outcome = ScenarioRunner.runWith(catalog, "a-pass", fake, fake.clocks)

		assertFailed(outcome, FailureKind.DRIVER_ERROR, stepIndex = -1)
		assertContains(outcome.message, "scripted driver failure in launch")
		assertContains(outcome.resultJson, "\"kind\":\"DRIVER_ERROR\"")
	}

	@Test
	fun run_whenTheDriverThrowsDuringAStep_isADriverErrorAtThatStep() {
		val fake = FakeDriver().apply { throwOn = "waitGone" }

		assertFailed(ScenarioRunner.runWith(catalog, "a-pass", fake, fake.clocks), FailureKind.DRIVER_ERROR, stepIndex = 0)
	}

	@Test
	fun run_whenTheDriverLogThrows_stillReturnsTheResultOfTheSteps() {
		val fake = FakeDriver().apply { logThrows = true }

		val outcome = ScenarioRunner.runWith(catalog, "a-pass", fake, fake.clocks)

		assertPassed(outcome)
		assertEquals(1, outcome.steps.size)
	}

	@Test
	fun run_forAnUnknownIdOrBrokenCatalog_neverThrows() {
		val unknown = ScenarioRunner.run(catalog, "nope", FakeDriver())
		val broken = ScenarioRunner.run("{ not json", "a-pass", FakeDriver())

		assertFailed(unknown, FailureKind.DRIVER_ERROR)
		assertContains(unknown.message, "Unknown scenario id: nope")
		assertFailed(broken, FailureKind.DRIVER_ERROR)
	}

	@Test
	fun ids_listsTheCatalogOrNothingWhenItCannotBeRead() {
		assertEquals(listOf("a-pass", "a-fail"), ScenarioRunner.ids(catalog))
		assertTrue(ScenarioRunner.ids("garbage").isEmpty())
	}

	@Test
	fun crashedOutcome_hasTheSameResultJsonShape() {
		val root = Json.parseToJsonElement(ScenarioOutcome.crashed("x", "boom").resultJson).jsonObject

		assertEquals("failed", root.getValue("outcome").jsonPrimitive.content)
		assertEquals("DRIVER_ERROR", root.getValue("failure").jsonObject.getValue("kind").jsonPrimitive.content)
	}
}
