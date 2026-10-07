package com.gdavidpb.tuindice.scenarios.catalog

/**
 * How many `onPlatform` branches a scenario may hold. A scenario not listed may hold none. The numbers start at
 * the count of platform conditions in the Maestro flow each scenario replaces, an upper bound: translating
 * removes most of them (the iOS paste patches and keystroke guards go away). Lower an entry when its batch lands;
 * raising one, or adding one, needs the owner's agreement.
 */
class PlatformBranchBudget private constructor() {
	companion object {
		val perScenario: Map<String, Int> = mapOf(
			"about-platform-edge-triggers" to 1,
			"auth-login-outdated-app" to 1,
			"auth-terms-privacy-from-login" to 4,
			"auth-update-password-failure" to 2,
			"auth-update-password" to 3,
			"maincore-back-stack" to 1,
			"maincore-browser-external-dialog" to 2,
			"maincore-tab-stack-preservation" to 1,
			"record-synthetic-term-lifecycle" to 1,
			"record-synthetic-term-rejected" to 1,
			"record-synthetic-term-search-states" to 3,
			"subjects-detail-failed-retry" to 2,
			"subjects-detail-tabs-tooltip" to 2,
			"subjects-detail-unavailable" to 2,
			"subjects-search-failed-retry" to 2,
			"subjects-search-query-clear" to 3,
			"subjects-smoke" to 3,
			"summary-profile-picture" to 2,
			"summary-refresh-retry" to 1
		)

		fun of(scenarioId: String): Int = perScenario[scenarioId] ?: 0
	}
}
