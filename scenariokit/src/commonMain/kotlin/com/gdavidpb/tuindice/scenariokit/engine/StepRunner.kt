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

	/** Whether a `WaitBackgrounded` that passed announced the exit and no `Foreground`/`Relaunch` closed it since. */
	private var exitAnnounced = false

	/** The step that ran last (its index in pre-order and the step itself): where the end of the scenario is checked. */
	private var lastRun: Pair<Int, Step>? = null

	fun runAll(steps: List<Step>): StepResult.Failed? = steps.firstNotNullOfOrNull { runOne(it) }

	/**
	 * Called once, when every step passed: a scenario must not end with the app out of the foreground unless a
	 * `WaitBackgrounded` said it would. The failure points at the last step that ran, as a step does.
	 */
	fun confirmEnd(): StepResult.Failed? {
		if (exitAnnounced) return null
		val (index, step) = lastRun ?: (-1 to null)
		val where = "after the last step of the scenario" + step?.let { " (${it.describe()})" }.orEmpty()
		val failure = runCatching { unannouncedExit(where) }.getOrElse { driverError(it) }
		return failure?.let { if (step == null) it else it.locatedAt(index, step) }
	}

	private fun runOne(step: Step): StepResult.Failed? {
		val index = recorder.begin(step)
		lastRun = index to step
		val mark = clocks.timeSource.markNow()
		val result = runCatching { beforeReturning(step) ?: dispatch(step) }.getOrElse { driverError(it) }
		announce(step, result)
		val durationMs = mark.elapsedNow().inWholeMilliseconds
		recorder.end(index, result.outcome, durationMs)
		// The log is evidence, never a reason to lose the run: a driver whose log throws still gets its result.
		val line = "[$index] ${step::class.simpleName} ${step.target} -> ${result.outcome.wire} ($durationMs ms)"
		runCatching { driver.log(line) }
		return (result as? StepResult.Failed)?.locatedAt(index, step)
	}

	/**
	 * A `Foreground` or a `Relaunch` brings the app back, so it would hide an exit nobody waited for: the app is confirmed
	 * in front first, unless the exit was announced. Null lets the step run.
	 */
	private fun beforeReturning(step: Step): StepResult.Failed? =
		if ((step is Step.Foreground || step is Step.Relaunch) && !exitAnnounced) {
			unannouncedExit("before ${step.describe()}")
		} else {
			null
		}

	/** A `WaitBackgrounded` that passed announces the exit; a `Foreground` or `Relaunch` that passed closes it. */
	private fun announce(step: Step, result: StepResult) {
		if (result !is StepResult.Passed) return
		when (step) {
			is Step.WaitBackgrounded -> exitAnnounced = true
			is Step.Foreground, is Step.Relaunch -> exitAnnounced = false
			else -> Unit
		}
	}

	private fun unannouncedExit(where: String): StepResult.Failed? =
		if (driver.confirmForeground()) {
			null
		} else {
			StepResult.Failed(
				FailureKind.APP_NOT_RUNNING,
				"the app left the foreground and no step waited for it (a WaitBackgrounded announces an exit on purpose); " +
					"found $where"
			)
		}

	private fun Step.describe(): String {
		val name = this::class.simpleName.orEmpty()
		return if (target.isEmpty()) name else "$name $target"
	}

	private fun dispatch(step: Step): StepResult = when (step) {
		is Step.Tap, is Step.TapAt, is Step.DoubleTap, is Step.Back, is Step.Swipe -> gestures.execute(step)
		is Step.EnterText, is Step.SubmitTextEntry -> text.execute(step)
		is Step.WaitVisible, is Step.WaitGone, is Step.WaitBackgrounded, is Step.AssertEnabled,
		is Step.AssertChecked, is Step.ScrollUntilVisible -> waits.execute(step)
		is Step.Relaunch, is Step.Foreground -> app.execute(step)
		is Step.ExpectRequest -> backend.expectRequest(step)
		is Step.SetMockState -> backend.setMockState(step)
		is Step.Container -> containers.execute(step)
	}

	private fun driverError(error: Throwable): StepResult.Failed =
		StepResult.Failed(FailureKind.DRIVER_ERROR, "the driver threw: ${error.stackTraceToString().take(STACK_LIMIT)}")

	private companion object {
		const val STACK_LIMIT = 2_000
	}
}
