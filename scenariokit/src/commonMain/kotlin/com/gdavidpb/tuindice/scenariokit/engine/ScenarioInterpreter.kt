package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.ScenarioFailure
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome

/**
 * Runs one scenario: prepares the backend, launches the app, then walks the steps and
 * stops at the first failure. Every step waits only for its own target; there is no
 * global idle wait.
 */
internal class ScenarioInterpreter(private val driver: ScenarioDriver, private val clocks: Clocks = Clocks()) {
	private val backend = BackendEngine(driver, Poller(driver, clocks.timeSource))

	fun run(scenario: Scenario): ScenarioOutcome {
		val startedAt = clocks.nowIso()
		val runner = StepRunner(driver, clocks, backend, scenario.start.arguments)
		val preparation = prepare(scenario)
		val failure = preparation.failure ?: stepFailure(scenario, runner)
		val report = failure?.let { reportFor(scenario, it) } ?: "Scenario ${scenario.id} passed"
		return ScenarioOutcome(
			scenarioId = scenario.id,
			startedAt = startedAt,
			finishedAt = clocks.nowIso(),
			steps = runner.recorder.all,
			failure = failure,
			report = report,
			prepareBackendMs = preparation.prepareBackendMs,
			launchMs = preparation.launchMs
		)
	}

	/** What `prepareBackend` and `launch` found and how long each took; a stage that never ran has no duration. */
	private class Preparation(val failure: ScenarioFailure?, val prepareBackendMs: Long?, val launchMs: Long?)

	private fun prepare(scenario: Scenario): Preparation {
		val prepareMark = clocks.timeSource.markNow()
		val backendError = backend.prepare(scenario.start)
		val prepareMs = prepareMark.elapsedNow().inWholeMilliseconds
		if (backendError != null) {
			return Preparation(beforeFirstStep(FailureKind.BACKEND_UNAVAILABLE, "prepareBackend", backendError), prepareMs, null)
		}

		val launchMark = clocks.timeSource.markNow()
		val launched = driver.launch(scenario.start)
		val launchMs = launchMark.elapsedNow().inWholeMilliseconds
		val failure = if (launched) null else beforeFirstStep(FailureKind.APP_NOT_RUNNING, "launch", "the app did not launch")
		return Preparation(failure, prepareMs, launchMs)
	}

	private fun beforeFirstStep(kind: FailureKind, primitive: String, message: String) =
		ScenarioFailure(kind, -1, primitive, "", message, "", "", null)

	private fun stepFailure(scenario: Scenario, runner: StepRunner): ScenarioFailure? =
		runner.runAll(scenario.steps)?.let { failed ->
			val refined = FailureRefiner.refine(failed, driver)
			ScenarioFailure(
				kind = refined.kind,
				stepIndex = refined.stepIndex,
				primitive = refined.step?.let { it::class.simpleName }.orEmpty(),
				target = refined.step?.target.orEmpty(),
				message = refined.message,
				expected = refined.expected,
				actual = refined.actual,
				site = refined.step?.site
			)
		}

	/** Captures the device's evidence and reads the backend once, only on failure. */
	private fun reportFor(scenario: Scenario, failure: ScenarioFailure): String {
		if (failure.kind != FailureKind.BACKEND_UNAVAILABLE) {
			runCatching { driver.captureFailure(scenario.id, failure.stepIndex) }
		}
		val snapshot = runCatching { backend.snapshot() }.getOrNull()
		return FailureReport.build(scenario.id, failure, snapshot)
	}
}
