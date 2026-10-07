package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.PlatformBranchBudget
import com.gdavidpb.tuindice.scenarios.catalog.RetryAllowlist
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The catalog does not grow retries or platform branches: both are how a scenario hides a defect. */
class CatalogBudgetTest {
	private val scenarios = E2eCatalog.all

	@Test
	fun noScenarioRetriesUnlessAllowedAndNoneIs() {
		assertTrue(RetryAllowlist.scenarioIds.isEmpty(), "the retry allowlist is empty by decision")

		val offenders = scenarios.filter { scenario ->
			scenario.id !in RetryAllowlist.scenarioIds && scenario.steps.flattened().any { it is Step.Retry }
		}

		assertTrue(offenders.isEmpty(), "scenarios that retry: ${offenders.map { it.id }}")
	}

	@Test
	fun theBudgetOnlyNamesScenariosOfAKnownModule() {
		val modules = E2eCatalog.byModule.keys

		PlatformBranchBudget.perScenario.forEach { (id, count) ->
			assertTrue(id.substringBefore('-') in modules, "budget for '$id' names no module")
			assertTrue(count > 0, "budget for '$id' is zero: drop the entry")
		}
	}

	@Test
	fun conformanceScenariosHoldNoPlatformBranchExceptTheBackOne() {
		scenarios.filter { it.id.startsWith("conformance-") && it.id != "conformance-back" }.forEach {
			assertEquals(0, branchesOf(it.steps), it.id)
		}
	}

	private fun branchesOf(steps: List<Step>): Int = steps.flattened().count { it is Step.OnPlatform }
}
