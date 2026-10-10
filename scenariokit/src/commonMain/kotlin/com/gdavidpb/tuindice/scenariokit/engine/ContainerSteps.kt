package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.Step

/**
 * Steps that run other steps. A guard (`IfVisible`, `OnPlatform`) that does not hold skips its children and
 * passes; children that fail still fail the scenario.
 */
internal class ContainerSteps(
	private val driver: ScenarioDriver,
	private val runAll: (List<Step>) -> StepResult.Failed?
) {
	fun execute(step: Step.Container): StepResult = when (step) {
		is Step.IfVisible -> runWhen(driver.waitVisible(step.q, step.withinMs), step)
		is Step.OnPlatform -> runWhen(driver.platform == step.platform, step)
		is Step.Group -> runWhen(true, step)
	}

	private fun runWhen(condition: Boolean, step: Step.Container): StepResult =
		if (condition) runAll(step.steps) ?: StepResult.Passed else StepResult.Skipped
}
