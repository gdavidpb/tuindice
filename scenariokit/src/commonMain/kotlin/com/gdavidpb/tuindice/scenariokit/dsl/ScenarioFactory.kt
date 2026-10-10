package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Scenario

/** Entry point of the DSL: `val X = scenario("auth-login-success", "auth", start) { ... }`. */
fun scenario(id: String, module: String, start: LaunchSpec, block: ScenarioBuilder.() -> Unit): Scenario =
	ScenarioBuilder(id, module, start).apply(block).toScenario()
