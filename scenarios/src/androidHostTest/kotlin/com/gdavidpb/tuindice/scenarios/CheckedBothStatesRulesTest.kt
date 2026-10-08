package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.dsl.assertChecked
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** K-3: a scenario that asserts a checked state asserts both states of the same element. */
class CheckedBothStatesRulesTest {
	private val clean = LaunchSpec(emptyMap())

	@Test
	fun everyScenarioThatAssertsACheckedStateAssertsBothStatesOfTheSameElement() {
		val offenders = CatalogToleranceRules.checkedAssertedInOneStateOnly(E2eCatalog.all)

		assertTrue(offenders.isEmpty(), "elements asserted checked in one state only: $offenders")
		assertTrue(
			E2eCatalog.all.any { scenario -> scenario.steps.flattened().any { it is Step.AssertChecked } },
			"the rule has nothing to read if no scenario asserts a checked state"
		)
	}

	@Test
	fun anAssertionOfOneStateOnlyIsCaughtAndTwoStatesOfTheSameElementAreNot() {
		val both = scenario("x-both", "x", clean) {
			assertChecked("a", false)
			tap("a")
			assertChecked("a", true)
		}
		val onlyAfter = scenario("x-after", "x", clean) {
			tap("a")
			assertChecked("a", true)
		}
		val twoElements = scenario("x-two", "x", clean) {
			assertChecked("a", false)
			assertChecked("b", true)
		}
		val nested = scenario("x-nested", "x", clean) {
			assertChecked("a", false)
			onPlatform(Platform.Android) { assertChecked("a", true) }
		}

		assertEquals(
			listOf("x-after: tag:a", "x-two: tag:a", "x-two: tag:b"),
			CatalogToleranceRules.checkedAssertedInOneStateOnly(listOf(both, onlyAfter, twoElements, nested))
		)
	}
}
