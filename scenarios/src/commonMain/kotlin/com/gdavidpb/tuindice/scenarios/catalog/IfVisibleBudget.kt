package com.gdavidpb.tuindice.scenarios.catalog

/**
 * How many `ifVisible` steps a scenario holds, exactly (`CatalogRulesTest` compares with equality). A scenario not
 * listed holds none, and none is listed today: an `ifVisible` never fails by itself, so it is a way to tolerate
 * what a scenario should assert. It is only for a step whose outcome the scenario cannot know, which has to be
 * argued with the owner before an entry is added here.
 */
class IfVisibleBudget private constructor() {
	companion object {
		val perScenario: Map<String, Int> = emptyMap()

		fun of(scenarioId: String): Int = perScenario[scenarioId] ?: 0
	}
}
