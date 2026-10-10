package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.describe

/**
 * The rules about what a scenario may tolerate or leave unwatched, as functions that return what breaks them, so
 * `CatalogRulesTest` runs each on the catalog (nothing breaks it) and on a scenario made to break it.
 */
internal object CatalogToleranceRules {
	private const val HTTP_OK = 200

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

	/**
	 * The ids among [ids] whose scenario does not wait for the resend of the pending change to be accepted
	 * (`POST /evaluations/v3` answered 200) or does not check that "sign out anyway" is not offered.
	 */
	fun flushesNotShownAccepted(scenarios: List<Scenario>, ids: Set<String>): List<String> =
		scenarios.filter { it.id in ids }.mapNotNull { scenario ->
			val steps = scenario.steps.flattened()
			val accepted = steps.any {
				it is Step.ExpectRequest && it.method == "POST" && it.path == "/evaluations/v3" && it.status == HTTP_OK
			}
			val notOffered = steps.any { it is Step.WaitGone && it.q == Query.Tag(AuthUiTags.SignOutSecondaryButton) }

			scenario.id.takeUnless { accepted && notOffered }
		}

	/**
	 * `scenario (platform): element` for every element that a scenario asserts checked in only one state on a platform
	 * it runs on. On iOS an unchecked box is not told apart from an element that is no toggle at all (a `Button`
	 * without the `Selected` trait reads "unchecked"), so an `assertChecked(x, false)` alone proves nothing and
	 * `assertChecked(x, true)` alone proves only the state after a change: a scenario asserts both states of the same
	 * element, on each platform it runs on (YB-7): a state asserted only inside the branch of the other platform is
	 * not asserted here.
	 */
	fun checkedAssertedInOneStateOnly(scenarios: List<Scenario>): List<String> =
		scenarios.flatMap { scenario ->
			scenario.platforms.flatMap { platform ->
				scenario.steps.reachableOn(platform).filterIsInstance<Step.AssertChecked>()
					.groupBy { it.q }
					.filterValues { asserts -> asserts.map { it.checked }.toSet().size < 2 }
					.keys
					.map { "${scenario.id} (${platform.name}): ${it.describe()}" }
			}
		}

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
