package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Step

internal class AppSteps(private val driver: ScenarioDriver, private val startArguments: Map<String, String>) {
	fun execute(step: Step): StepResult = when (step) {
		is Step.Relaunch -> passIf(driver.launch(LaunchSpec(argumentsOf(step))), FailureKind.APP_NOT_RUNNING) {
			"the app could not be relaunched"
		}
		is Step.Foreground -> passIf(driver.foreground(), FailureKind.APP_NOT_RUNNING) {
			"the app could not be brought to the foreground"
		}
		else -> unhandled(step)
	}

	/** A relaunch that names no arguments starts the app the way the scenario started it. */
	private fun argumentsOf(step: Step.Relaunch): Map<String, String> = step.arguments.ifEmpty { startArguments }
}
