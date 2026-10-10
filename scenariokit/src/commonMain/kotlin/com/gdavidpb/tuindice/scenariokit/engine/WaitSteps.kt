package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step

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
		is Step.WaitBackgrounded -> waitBackgrounded(step)
		is Step.AssertChecked -> assertChecked(step)
		is Step.AssertEnabled -> assertEnabled(step)
		is Step.ScrollUntilVisible -> scroll.execute(step)
		else -> unhandled(step)
	}

	/**
	 * The app is out of the foreground on [BACKGROUND_READS] reads in a row, one poll interval apart. An app whose process
	 * is gone is not out of the foreground, it is dead: the read that finds it so ends the step with `APP_NOT_RUNNING`.
	 */
	private fun waitBackgrounded(step: Step.WaitBackgrounded): StepResult {
		var inARow = 0
		var dead = false
		val done = poller.until(step.timeoutMs) {
			dead = !driver.isRunning()
			inARow = if (dead || driver.isForeground()) 0 else inARow + 1
			dead || inARow >= BACKGROUND_READS
		}
		return when {
			dead -> StepResult.Failed(
				FailureKind.APP_NOT_RUNNING,
				"the app is not running, so it did not go to the background: it ended"
			)
			else -> passIf(done, FailureKind.STEP_TIMEOUT) { "the app was still in the foreground after ${step.timeoutMs} ms" }
		}
	}

	/** Polls until the checkbox is on screen with the checked state the step asks for. */
	private fun assertChecked(step: Step.AssertChecked): StepResult {
		val matched = poller.until(step.timeoutMs) { driver.isVisible(step.q) && driver.isChecked(step.q) == step.checked }
		return passIf(matched, FailureKind.STEP_TIMEOUT) {
			"${step.target} did not become ${if (step.checked) "checked" else "unchecked"} within ${step.timeoutMs} ms"
		}
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

	private companion object {
		const val BACKGROUND_READS = 3
	}
}
