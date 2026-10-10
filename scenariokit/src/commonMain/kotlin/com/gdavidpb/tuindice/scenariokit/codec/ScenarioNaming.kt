package com.gdavidpb.tuindice.scenariokit.codec

import com.gdavidpb.tuindice.scenariokit.model.Scenario

/** Sole owner of how a scenario is named on each platform; the Swift generator and the harness both use it. */
object ScenarioNaming {
	const val IOS_TARGET = "TuIndiceUITests"

	/** `auth` -> `AuthScenarioTests`: one XCTestCase class per module. */
	fun swiftClassName(module: String): String = module.replaceFirstChar { it.uppercaseChar() } + "ScenarioTests"

	/** `auth-login-success` -> `test_auth_login_success`. */
	fun swiftMethodName(id: String): String = "test_" + id.replace('-', '_')

	/** The `-only-testing` value that selects exactly this scenario. */
	fun iosOnlyTesting(scenario: Scenario): String =
		"$IOS_TARGET/${swiftClassName(scenario.module)}/${swiftMethodName(scenario.id)}"

	/** The value of the `scenario` instrumentation argument. */
	fun androidScenarioArg(scenario: Scenario): String = scenario.id
}
