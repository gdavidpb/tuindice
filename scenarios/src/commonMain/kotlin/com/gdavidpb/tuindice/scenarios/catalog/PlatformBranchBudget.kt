package com.gdavidpb.tuindice.scenarios.catalog

/**
 * How many `onPlatform` branches a scenario holds, exactly (`CatalogRulesTest` compares with equality). A scenario
 * not listed holds none. Lowering an entry when a branch goes away is part of removing it; raising one, or
 * adding one, needs the owner's agreement.
 */
class PlatformBranchBudget private constructor() {
	companion object {
		val perScenario: Map<String, Int> = mapOf(
			"about-platform-edge-triggers" to 4,
			"auth-terms-privacy-from-login" to 4,
			"conformance-system" to 2,
			"maincore-tab-stack-preservation" to 1,
			"summary-profile-picture" to 2,
			"summary-profile-picture-sources" to 4
		)

		fun of(scenarioId: String): Int = perScenario[scenarioId] ?: 0
	}
}
