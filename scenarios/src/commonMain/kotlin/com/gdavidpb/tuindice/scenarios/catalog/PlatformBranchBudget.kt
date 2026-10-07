package com.gdavidpb.tuindice.scenarios.catalog

/**
 * How many `onPlatform` branches a scenario holds, exactly (`CatalogRulesTest` compares with equality). A scenario
 * not listed holds none. Lowering an entry when a branch goes away is part of removing it; raising one, or
 * adding one, needs the owner's agreement.
 */
class PlatformBranchBudget private constructor() {
	companion object {
		val perScenario: Map<String, Int> = mapOf(
			"about-platform-edge-triggers" to 1,
			"auth-terms-privacy-from-login" to 4,
			"auth-update-password" to 1,
			"maincore-back-stack" to 1,
			"maincore-browser-external-dialog" to 2,
			"maincore-tab-stack-preservation" to 1,
			"record-synthetic-term-lifecycle" to 1,
			"record-synthetic-term-rejected" to 1,
			"record-synthetic-term-search-states" to 3,
			"subjects-detail-failed-retry" to 1,
			"subjects-detail-tabs-tooltip" to 1,
			"subjects-detail-unavailable" to 1,
			"subjects-search-failed-retry" to 1,
			"subjects-search-query-clear" to 1,
			"subjects-smoke" to 1,
			"summary-profile-picture" to 2,
			"summary-refresh-retry" to 1
		)

		fun of(scenarioId: String): Int = perScenario[scenarioId] ?: 0
	}
}
