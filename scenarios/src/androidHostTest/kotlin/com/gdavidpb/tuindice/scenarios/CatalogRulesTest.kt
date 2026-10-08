package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.PlatformBranchBudget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The rules of how a scenario is shaped (see `CatalogRules`): the platform branches, `back` and the platforms a
 * scenario runs on. Each is checked on the catalog and then on a scenario made to break it, so a rule that
 * stops detecting anything fails here. `CatalogContentRulesTest` has the rules about what a scenario says.
 */
class CatalogRulesTest {
	private val scenarios = E2eCatalog.all
	private val clean = LaunchSpec(emptyMap())

	/** Scenarios that run on one platform on purpose: the conformance one of `back`, which iOS does not have. */
	private val platformRestricted = setOf("conformance-back", "conformance-hide-keyboard")

	// The platform branches match the budget exactly.

	@Test
	fun everyScenarioHoldsExactlyTheBranchesOfItsBudget() {
		val mismatches = CatalogRules.branchBudgetMismatches(scenarios, PlatformBranchBudget::of)

		assertTrue(mismatches.isEmpty(), "branches and budget differ: $mismatches")
	}

	@Test
	fun theBranchBudgetCatchesSlackAndExcess() {
		val branching = scenario("x-one", "x", clean) { onPlatform(Platform.Android) { tap("a") } }
		val plain = scenario("x-none", "x", clean) { tap("a") }

		assertEquals(1, CatalogRules.branchBudgetMismatches(listOf(branching), { 0 }).size, "a branch over a zero budget")
		assertEquals(1, CatalogRules.branchBudgetMismatches(listOf(plain), { 1 }).size, "a budget nobody uses")
		assertTrue(CatalogRules.branchBudgetMismatches(listOf(branching, plain), { if (it == "x-one") 1 else 0 }).isEmpty())
	}

	// Back is Android only.

	@Test
	fun backOnlyRunsOnAndroid() {
		val offenders = CatalogRules.backOutsideAndroid(scenarios)

		assertTrue(offenders.isEmpty(), "scenarios that press back where iOS has none: $offenders")
	}

	@Test
	fun backOutsideAnAndroidBranchIsCaught() {
		val everywhere = scenario("x-back", "x", clean) { back() }
		val underIos = scenario("x-ios", "x", clean) { onPlatform(Platform.Ios) { back() } }
		val underAndroid = scenario("x-android", "x", clean) { onPlatform(Platform.Android) { back() } }
		val androidOnly = scenario("x-only", "x", clean) {
			platforms(Platform.Android)
			back()
		}

		assertEquals(
			listOf("x-back", "x-ios"),
			CatalogRules.backOutsideAndroid(listOf(everywhere, underIos, underAndroid, androidOnly))
		)
	}

	// Platform restriction has a list.

	@Test
	fun onlyTheListedScenariosSkipAPlatform() {
		val offenders = CatalogRules.restrictedPlatformsOutside(scenarios, platformRestricted)

		assertTrue(offenders.isEmpty(), "scenarios that drop a platform without being listed: $offenders")
		assertTrue(
			scenarios.filter { it.platforms != Platform.entries }.map { it.id }.toSet() == platformRestricted,
			"the list names a scenario that runs everywhere"
		)
	}

	@Test
	fun aScenarioThatDropsAPlatformIsCaught() {
		val android = scenario("x-android", "x", clean) {
			platforms(Platform.Android)
			tap("a")
		}

		assertEquals(listOf("x-android"), CatalogRules.restrictedPlatformsOutside(listOf(android), emptySet()))
		assertTrue(CatalogRules.restrictedPlatformsOutside(listOf(android), setOf("x-android")).isEmpty())
	}
}
