package com.gdavidpb.tuindice.scenarios.catalog

/**
 * How many `onPlatform` branches a scenario holds, exactly (`CatalogRulesTest` compares with equality). A scenario
 * not listed holds none. Lowering an entry when a branch goes away is part of removing it; raising one, or
 * adding one, needs the owner's agreement.
 */
class PlatformBranchBudget private constructor() {
	companion object {
		val perScenario: Map<String, Int> = mapOf(
			"about-platform-edge-triggers" to 3,
			"auth-terms-privacy-from-login" to 4,
			"auth-update-password" to 1,
			"maincore-tab-stack-preservation" to 1,
			"summary-profile-picture" to 2
		)

		fun of(scenarioId: String): Int = perScenario[scenarioId] ?: 0
	}
}
