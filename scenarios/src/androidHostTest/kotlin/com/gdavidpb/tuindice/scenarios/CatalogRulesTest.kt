package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.group
import com.gdavidpb.tuindice.scenariokit.dsl.ifVisible
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.submitTextEntry
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.IfVisibleBudget
import com.gdavidpb.tuindice.scenarios.catalog.PlatformBranchBudget
import com.gdavidpb.tuindice.scenarios.shared.Within
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

	/**
	 * Scenarios that run on one platform on purpose: the conformance one of `back`, which iOS does not have, and the one
	 * of the effect of the double tap, which no XCUITest delivery of the gesture shows on the iOS simulator.
	 */
	private val platformRestricted = setOf("conformance-back", "conformance-double-tap-effect")

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

	// ifVisible tolerates; its budget is exact too.

	@Test
	fun everyScenarioHoldsExactlyTheIfVisibleStepsOfItsBudget() {
		val mismatches = CatalogToleranceRules.ifVisibleBudgetMismatches(scenarios, IfVisibleBudget::of)

		assertTrue(mismatches.isEmpty(), "ifVisible steps and budget differ: $mismatches")
	}

	@Test
	fun theIfVisibleBudgetCatchesSlackAndExcessAlsoInsideOtherSteps() {
		val tolerant = scenario("x-tolerant", "x", clean) { ifVisible("a") { tap("b") } }
		val nested = scenario("x-nested", "x", clean) { onPlatform(Platform.Ios) { ifVisible("a") { tap("b") } } }
		val plain = scenario("x-plain", "x", clean) { tap("a") }
		val budgetOfTolerant = { id: String -> if (id == "x-tolerant") 1 else 0 }

		assertEquals(1, CatalogToleranceRules.ifVisibleBudgetMismatches(listOf(tolerant)) { 0 }.size, "over a zero budget")
		assertEquals(1, CatalogToleranceRules.ifVisibleBudgetMismatches(listOf(nested)) { 0 }.size, "under a branch")
		assertEquals(1, CatalogToleranceRules.ifVisibleBudgetMismatches(listOf(plain)) { 1 }.size, "a budget nobody uses")
		assertTrue(CatalogToleranceRules.ifVisibleBudgetMismatches(listOf(tolerant, plain), budgetOfTolerant).isEmpty())
	}

	// A credential request is the one the step before it causes.

	@Test
	fun everyRequestWithACredentialFollowsTheTapOrTheSubmitThatCausesIt() {
		val offenders = CatalogToleranceRules.expectedCredentialRequestsNotRightAfterTheGesture(scenarios)

		assertTrue(offenders.isEmpty(), "requests with a credential that do not follow a tap or a submit: $offenders")
	}

	@Test
	fun aCredentialRequestAwayFromItsGestureIsCaughtAndTheAdjacentOnesAreNot() {
		val afterTap = scenario("x-tap", "x", clean) {
			tap("a")
			expectRequest("POST", "/auth/v1/token", basicAuth = "u:p")
		}
		val afterSubmit = scenario("x-submit", "x", clean) {
			submitTextEntry()
			expectRequest("POST", "/auth/v1/token", basicAuth = "u:p")
		}
		val insideGroup = scenario("x-group", "x", clean) {
			group("g") {
				tap("a")
				expectRequest("POST", "/auth/v1/token", basicAuth = "u:p")
			}
		}
		val afterWait = scenario("x-wait", "x", clean) {
			tap("a")
			waitVisible("loading", Within.Assert)
			expectRequest("POST", "/auth/v1/token", basicAuth = "u:p")
		}
		val first = scenario("x-first", "x", clean) { expectRequest("POST", "/auth/v1/token", basicAuth = "u:p") }
		val withoutCredential = scenario("x-plain", "x", clean) {
			tap("a")
			waitVisible("loading", Within.Assert)
			expectRequest("POST", "/evaluations/v3")
		}

		assertEquals(
			listOf("x-wait: ExpectRequest POST /auth/v1/token", "x-first: ExpectRequest POST /auth/v1/token"),
			CatalogToleranceRules.expectedCredentialRequestsNotRightAfterTheGesture(
				listOf(afterTap, afterSubmit, insideGroup, afterWait, first, withoutCredential)
			)
		)
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
