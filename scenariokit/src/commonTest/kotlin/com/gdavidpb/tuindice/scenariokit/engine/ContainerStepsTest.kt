package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.StepOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContainerStepsTest {
	private val button: Query = Query.Tag("button")
	private val banner: Query = Query.Tag("banner")
	private val tapButton = Step.Tap(button)

	private fun driver(vararg elements: Pair<Query, FakeElement>, platform: Platform = Platform.Android) =
		FakeDriver(platform).apply { screen.putAll(elements) }

	@Test
	fun ifVisible_neverFailsWhenTheElementIsAbsent() {
		val fake = driver()

		val outcome = fake.run(Step.IfVisible(banner, 1_500, listOf(tapButton)))

		assertPassed(outcome)
		assertEquals(StepOutcome.Skipped, outcome.steps.single().outcome)
		assertTrue(fake.taps.isEmpty())
	}

	@Test
	fun ifVisible_runsItsStepsWhenTheElementShowsUp() {
		val fake = driver(banner to FakeElement(), button to FakeElement())

		assertPassed(fake.run(Step.IfVisible(banner, 1_500, listOf(tapButton))))
		assertEquals(listOf(button), fake.taps)
	}

	@Test
	fun ifVisible_whenItsStepsFail_failsAtTheInnerStep() {
		val fake = driver(banner to FakeElement())

		val failure = assertFailed(fake.run(Step.IfVisible(banner, 1_500, listOf(tapButton))), FailureKind.STEP_TIMEOUT)

		assertEquals(1, failure.stepIndex)
		assertEquals("Tap", failure.primitive)
	}

	@Test
	fun onPlatform_runsOnlyOnTheMatchingPlatform() {
		val android = driver(button to FakeElement())
		val ios = driver(button to FakeElement(), platform = Platform.Ios)

		assertPassed(android.run(Step.OnPlatform(Platform.Ios, listOf(tapButton))))
		assertTrue(android.taps.isEmpty())
		assertPassed(ios.run(Step.OnPlatform(Platform.Ios, listOf(tapButton))))
		assertEquals(listOf(button), ios.taps)
	}

	@Test
	fun group_runsItsStepsAndAppearsInTheRecords() {
		val fake = driver(button to FakeElement())

		val outcome = fake.run(Step.Group("sign in", listOf(tapButton)))

		assertPassed(outcome)
		assertEquals(listOf("Group", "Tap"), outcome.steps.map { it.primitive })
		assertEquals("sign in", outcome.steps.first().target)
	}
}
