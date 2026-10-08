package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step

/** Runs steps in order, recording each one, and stops at the first failure. */
internal class StepRunner(
	private val driver: ScenarioDriver,
	private val clocks: Clocks,
	private val backend: BackendEngine,
	startArguments: Map<String, String> = emptyMap(),
	val recorder: StepRecorder = StepRecorder()
) {
	private val poller = Poller(driver, clocks.timeSource)
	private val gestures = GestureSteps(driver, poller, clocks.timeSource)
	private val text = TextSteps(driver, poller)
	private val waits = WaitSteps(driver, poller, ScrollEngine(driver, poller))
	private val app = AppSteps(driver, startArguments)
	private val containers = ContainerSteps(driver, ::runAll)

	fun runAll(steps: List<Step>): StepResult.Failed? = steps.firstNotNullOfOrNull { runOne(it) }

	private fun runOne(step: Step): StepResult.Failed? {
		val index = recorder.begin(step)
		val mark = clocks.timeSource.markNow()
		val result = runCatching { dispatch(step) }.getOrElse { driverError(it) }
		val durationMs = mark.elapsedNow().inWholeMilliseconds
		recorder.end(index, result.outcome, durationMs)
		// The log is evidence, never a reason to lose the run: a driver whose log throws still gets its result.
		runCatching { driver.log("[$index] ${step::class.simpleName} ${step.target} -> ${result.outcome.wire} ($durationMs ms)") }
		return (result as? StepResult.Failed)?.locatedAt(index, step)
	}

	private fun dispatch(step: Step): StepResult = when (step) {
		is Step.Tap, is Step.TapAt, is Step.DoubleTap, is Step.Back, is Step.Swipe -> gestures.execute(step)
		is Step.EnterText, is Step.ClearText, is Step.FinishTextEntry -> text.execute(step)
		is Step.WaitVisible, is Step.WaitGone, is Step.WaitAnyVisible, is Step.AssertEnabled,
		is Step.Settle, is Step.ScrollUntilVisible -> waits.execute(step)
		is Step.Relaunch, is Step.Foreground -> app.execute(step)
		is Step.ExpectRequest -> backend.expectRequest(step)
		is Step.Container -> containers.execute(step)
	}

	private fun driverError(error: Throwable): StepResult.Failed =
		StepResult.Failed(FailureKind.DRIVER_ERROR, "the driver threw: ${error.stackTraceToString().take(STACK_LIMIT)}")

	private companion object {
		const val STACK_LIMIT = 2_000
	}
}
