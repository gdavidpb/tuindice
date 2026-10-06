package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

internal class WaitSteps(
	private val driver: ScenarioDriver,
	private val poller: Poller,
	private val scroll: ScrollEngine
) {
	fun execute(step: Step): StepResult = when (step) {
		is Step.WaitVisible -> passIf(driver.waitVisible(step.q, step.timeoutMs), FailureKind.STEP_TIMEOUT) {
			"${step.target} was not visible within ${step.timeoutMs} ms"
		}
		is Step.WaitGone -> passIf(driver.waitGone(step.q, step.timeoutMs), FailureKind.STEP_TIMEOUT) {
			"${step.target} was still visible after ${step.timeoutMs} ms"
		}
		is Step.WaitAnyVisible -> waitAnyVisible(step)
		is Step.AssertEnabled -> assertEnabled(step)
		is Step.Settle -> settle(step)
		is Step.ScrollUntilVisible -> scroll.execute(step)
		else -> unhandled(step)
	}

	private fun waitAnyVisible(step: Step.WaitAnyVisible): StepResult {
		val shown = poller.until(step.timeoutMs) { step.queries.any { driver.isVisible(it) } }
		return passIf(shown, FailureKind.STEP_TIMEOUT) { "none of ${step.target} was visible within ${step.timeoutMs} ms" }
	}

	/** Polls until the element's enabled state matches; a missing element never satisfies either state. */
	private fun assertEnabled(step: Step.AssertEnabled): StepResult {
		val matched = poller.until(step.timeoutMs) {
			driver.isVisible(step.q) && driver.isEnabled(step.q) == step.enabled
		}
		return passIf(matched, FailureKind.STEP_TIMEOUT) {
			"${step.target} did not become ${if (step.enabled) "enabled" else "disabled"} within ${step.timeoutMs} ms"
		}
	}

	/** Settled means two bounds reads, [Timeouts.SettleInterval] apart, came back equal. */
	private fun settle(step: Step.Settle): StepResult {
		var previous = driver.bounds(step.q)
		val settled = poller.until(step.timeoutMs, Timeouts.SettleInterval) {
			val current = driver.bounds(step.q)
			val stable = current != null && current == previous
			previous = current
			stable
		}
		return passIf(settled, FailureKind.STEP_TIMEOUT) { "${step.target} did not settle within ${step.timeoutMs} ms" }
	}
}
