package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Step

/**
 * The rules about what a scenario may tolerate or leave unwatched, as functions that return what breaks them, so
 * `CatalogRulesTest` runs each on the catalog (nothing breaks it) and on a scenario made to break it.
 */
internal object CatalogToleranceRules {
	/** Scenarios whose `ifVisible` steps differ from their budget, in either direction. */
	fun ifVisibleBudgetMismatches(scenarios: List<Scenario>, budgetOf: (String) -> Int): List<String> =
		scenarios.mapNotNull { scenario ->
			val conditionals = scenario.steps.flattened().count { it is Step.IfVisible }

			"${scenario.id}: $conditionals ifVisible, budget ${budgetOf(scenario.id)}"
				.takeIf { conditionals != budgetOf(scenario.id) }
		}

	/**
	 * `scenario: step` for every `expectRequest` with a credential that does not directly follow a tap or a
	 * submit: the request it looks for is the one that step causes, and anything in between (a wait, a read of
	 * the screen) lets a slow mock answer before the scenario looks at what the app shows while it waits.
	 */
	fun expectedCredentialRequestsNotRightAfterTheGesture(scenarios: List<Scenario>): List<String> =
		scenarios.flatMap { scenario -> credentialRequestsAway(scenario.steps).map { "${scenario.id}: $it" } }

	private fun credentialRequestsAway(steps: List<Step>): List<String> {
		val here = steps.mapIndexedNotNull { index, step ->
			val after = steps.getOrNull(index - 1)

			"ExpectRequest ${step.target}".takeIf {
				step is Step.ExpectRequest && step.basicAuth != null && after !is Step.Tap && after !is Step.SubmitTextEntry
			}
		}

		return here + steps.filterIsInstance<Step.Container>().flatMap { credentialRequestsAway(it.steps) }
	}
}
