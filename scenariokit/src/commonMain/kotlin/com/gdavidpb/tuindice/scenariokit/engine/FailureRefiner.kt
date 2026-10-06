package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.SYSTEM_QUERY_PREFIX
import com.gdavidpb.tuindice.scenariokit.model.Step

/**
 * Turns a generic step failure into an environment one when the driver can see why:
 * an OS dialog in front of the app, or the app no longer in the foreground.
 */
internal object FailureRefiner {
	private val GENERIC = setOf(FailureKind.ASSERTION, FailureKind.STEP_TIMEOUT)

	fun refine(failure: StepResult.Failed, driver: ScenarioDriver): StepResult.Failed {
		val dialog = if (failure.kind in GENERIC) runCatching { driver.systemDialogInFront() }.getOrNull() else null
		return when {
			failure.kind !in GENERIC -> failure
			dialog != null -> failure.copy(
				kind = FailureKind.SYSTEM_DIALOG,
				message = "${failure.message} (system dialog in front: $dialog)"
			)
			needsApp(failure.step) && appLeftForeground(driver) -> failure.copy(
				kind = FailureKind.APP_NOT_RUNNING,
				message = "${failure.message} (the app is not in the foreground)"
			)
			else -> failure
		}
	}

	private fun appLeftForeground(driver: ScenarioDriver): Boolean =
		runCatching { !driver.isForeground() }.getOrDefault(false)

	/** Everything except launch control, backend checks and elements of the OS itself. */
	private fun needsApp(step: Step?): Boolean = when (step) {
		null, is Step.Relaunch, is Step.Foreground, is Step.ExpectRequest, is Step.Container -> false
		else -> !step.target.contains(SYSTEM_QUERY_PREFIX)
	}
}
