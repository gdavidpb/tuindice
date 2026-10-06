package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Step

internal class AppSteps(private val driver: ScenarioDriver) {
	fun execute(step: Step): StepResult = when (step) {
		is Step.Relaunch -> passIf(driver.launch(LaunchSpec(step.arguments)), FailureKind.APP_NOT_RUNNING) {
			"the app could not be relaunched"
		}
		is Step.Foreground -> passIf(driver.foreground(), FailureKind.APP_NOT_RUNNING) {
			"the app could not be brought to the foreground"
		}
		else -> unhandled(step)
	}
}
