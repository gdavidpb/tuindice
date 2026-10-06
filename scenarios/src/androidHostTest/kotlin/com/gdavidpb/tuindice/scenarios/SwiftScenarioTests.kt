package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.codec.ScenarioNaming
import com.gdavidpb.tuindice.scenariokit.model.Scenario

/** Renders the XCTestCase classes, one per module and one method per scenario, in a fixed order. */
internal object SwiftScenarioTests {
	const val HEADER = "// Generated from the scenario catalog by ./gradlew syncE2eArtifacts. Do not edit."

	fun render(scenarios: List<Scenario>): String {
		val classes = scenarios.groupBy { it.module }.toSortedMap().map { (module, ofModule) ->
			val methods = ofModule.sortedBy { it.id }.joinToString("\n") { scenario ->
				"    func ${ScenarioNaming.swiftMethodName(scenario.id)}() { runScenario(\"${scenario.id}\") }"
			}

			"final class ${ScenarioNaming.swiftClassName(module)}: ScenarioTestCase {\n$methods\n}"
		}

		return "$HEADER\nimport XCTest\n\n${classes.joinToString("\n\n")}\n"
	}
}
