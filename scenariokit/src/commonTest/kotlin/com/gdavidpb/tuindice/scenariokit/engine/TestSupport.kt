package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

fun scenarioOf(vararg steps: Step, start: LaunchSpec = LaunchSpec(emptyMap()), id: String = "test-scenario") =
	Scenario(id = id, module = "test", start = start, steps = steps.toList())

internal fun FakeDriver.run(scenario: Scenario): ScenarioOutcome = ScenarioInterpreter(this, clocks).run(scenario)

internal fun FakeDriver.run(vararg steps: Step): ScenarioOutcome = run(scenarioOf(*steps))

fun assertPassed(outcome: ScenarioOutcome) {
	assertNull(outcome.failure, outcome.report)
}

fun assertFailed(outcome: ScenarioOutcome, kind: FailureKind, stepIndex: Int? = null) =
	assertNotNull(outcome.failure, "expected a $kind failure but the scenario passed").also {
		assertEquals(kind, it.kind, outcome.report)
		if (stepIndex != null) assertEquals(stepIndex, it.stepIndex, outcome.report)
	}
