package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import com.gdavidpb.tuindice.scenariokit.model.describe
import kotlin.time.TimeSource

internal class GestureSteps(
	private val driver: ScenarioDriver,
	private val poller: Poller,
	private val timeSource: TimeSource
) {
	fun execute(step: Step): StepResult = when (step) {
		is Step.Tap -> tap(step)
		is Step.TapAt -> driver.awaitTarget(step.q)
			?: passIf(driver.tapAt(step.q, step.fx, step.fy), FailureKind.ASSERTION) { "tapAt ${step.target} was refused" }
		is Step.DoubleTap -> driver.awaitTarget(step.q)
			?: passIf(driver.doubleTap(step.q), FailureKind.ASSERTION) { "doubleTap ${step.target} was refused" }
		is Step.Back -> passIf(driver.pressBack(), FailureKind.ASSERTION) { "back was not handled" }
		is Step.Swipe -> driver.awaitTarget(step.from)
			?: passIf(
				driver.swipe(step.from, SwipeVector(step.fx, step.fy, step.dx, step.dy), step.durationMs),
				FailureKind.ASSERTION
			) { "swipe from ${step.target} was refused" }
		else -> unhandled(step)
	}

	/**
	 * A tap needs the target visible and, unless the step opts out, enabled, both within one
	 * [Timeouts.Action] budget: a control that enables itself when a load ends is normal UI.
	 * The gesture is made once; a target still disabled when the budget runs out is a failure.
	 */
	private fun tap(step: Step.Tap): StepResult {
		val mark = timeSource.markNow()

		return driver.awaitTarget(step.q)
			?: awaitEnabled(step, Timeouts.Action - mark.elapsedNow().inWholeMilliseconds)
			?: passIf(driver.tap(step.q), FailureKind.ASSERTION) { "tap on ${step.q.describe()} was refused" }
	}

	private fun awaitEnabled(step: Step.Tap, remainingMs: Long): StepResult.Failed? =
		if (step.requireEnabled && !poller.until(remainingMs.coerceAtLeast(0)) { driver.isEnabled(step.q) }) {
			StepResult.Failed(
				FailureKind.ASSERTION,
				"${step.q.describe()} is disabled and cannot be tapped; still disabled after ${Timeouts.Action} ms",
				expected = "enabled",
				actual = "disabled"
			)
		} else {
			null
		}
}
