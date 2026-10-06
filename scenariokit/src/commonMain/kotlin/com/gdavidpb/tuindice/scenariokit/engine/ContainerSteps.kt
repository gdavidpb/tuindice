package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step

/**
 * Steps that run other steps. A guard (`IfVisible`, `IfGone`, `OnPlatform`) that does not
 * hold skips its children and passes; children that fail still fail the scenario.
 */
internal class ContainerSteps(
	private val driver: ScenarioDriver,
	private val runAll: (List<Step>) -> StepResult.Failed?
) {
	fun execute(step: Step.Container): StepResult = when (step) {
		is Step.IfVisible -> runWhen(driver.waitVisible(step.q, step.withinMs), step)
		is Step.IfGone -> runWhen(driver.waitGone(step.q, step.withinMs), step)
		is Step.OnPlatform -> runWhen(driver.platform == step.platform, step)
		is Step.Retry -> retry(step)
		is Step.Group -> runWhen(true, step)
	}

	private fun runWhen(condition: Boolean, step: Step.Container): StepResult =
		if (condition) runAll(step.steps) ?: StepResult.Passed else StepResult.Skipped

	/** Only assertion failures and timeouts are worth another attempt; the cap is [Step.Retry.MAX_ATTEMPTS]. */
	private fun retry(step: Step.Retry): StepResult {
		val attempts = step.maxAttempts.coerceIn(1, Step.Retry.MAX_ATTEMPTS)
		var attempt = 0
		var failure: StepResult.Failed?
		do {
			attempt++
			failure = runAll(step.steps)
			if (failure != null) driver.log("retry '${step.reason}': attempt $attempt of $attempts failed")
		} while (failure != null && failure.kind in RETRIABLE && attempt < attempts)
		return failure ?: StepResult.Passed
	}

	private companion object {
		val RETRIABLE = setOf(FailureKind.ASSERTION, FailureKind.STEP_TIMEOUT)
	}
}
