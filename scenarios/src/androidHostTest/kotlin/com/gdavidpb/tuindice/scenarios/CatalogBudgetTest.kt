package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.IfVisibleBudget
import com.gdavidpb.tuindice.scenarios.catalog.PlatformBranchBudget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The catalog does not grow platform branches: they are how a scenario hides a defect. */
class CatalogBudgetTest {
	private val scenarios = E2eCatalog.all

	@Test
	fun theBudgetOnlyNamesScenariosOfAKnownModule() {
		val modules = E2eCatalog.byModule.keys

		PlatformBranchBudget.perScenario.forEach { (id, count) ->
			assertTrue(id.substringBefore('-') in modules, "budget for '$id' names no module")
			assertTrue(count > 0, "budget for '$id' is zero: drop the entry")
		}
	}

	@Test
	fun theIfVisibleBudgetOnlyNamesScenariosOfAKnownModule() {
		val modules = E2eCatalog.byModule.keys

		IfVisibleBudget.perScenario.forEach { (id, count) ->
			assertTrue(id.substringBefore('-') in modules, "ifVisible budget for '$id' names no module")
			assertTrue(count > 0, "ifVisible budget for '$id' is zero: drop the entry")
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
