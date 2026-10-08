package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Every coachmark of every screen is shown, in the order the screen lists them, by a `coachmarks-*` scenario. The
 * screens and their order are read from the product (`CoachmarkSurface.kt`), not written here, so a coachmark
 * added to a screen, or moved within it, fails this test until a scenario follows it, and so does a screen that
 * lists a coachmark another one already lists: the listings are counted, not merged.
 */
class CoachmarkCoverageTest {
	private val surfaceSource =
		RepoFiles.file("wizard/src/commonMain/kotlin/com/gdavidpb/tuindice/wizard/presentation/mapper/CoachmarkSurface.kt")
	private val clean = LaunchSpec(emptyMap())

	/** Each `listOf(CoachmarkId.A, CoachmarkId.B)` of the source: the coachmarks one screen lists, in order. */
	private val surfaces: List<List<CoachmarkId>> = Regex("""listOf\(([^)]*CoachmarkId\.[^)]*)\)""")
		.findAll(surfaceSource.readText())
		.map { match ->
			Regex("""CoachmarkId\.(\w+)""").findAll(match.groupValues[1]).map { CoachmarkId.valueOf(it.groupValues[1]) }.toList()
		}
		.toList()

	/**
	 * How many listings carry each coachmark. The record lists its two in two places (the content state and the
	 * route state, which only differ in when they are eligible); the others are listed once.
	 */
	private val listedTwice = setOf(CoachmarkId.Record, CoachmarkId.RecordControls)

	@Test
	fun theSurfacesReadFromTheProductListEveryCoachmarkAsManyTimesAsExpected() {
		assertEquals(emptyList(), listingMismatches(surfaces))
	}

	@Test
	fun everyScreenHasAScenarioThatShowsItsCoachmarksInOrder() {
		val awaited = awaitedBy(E2eCatalog.all)

		surfaces.forEach { surface ->
			assertTrue(
				showsInOrder(awaited, surface),
				"no coachmarks-* scenario waits for ${surface.map { it.name }} in that order"
			)
		}
	}

	@Test
	fun theCheckNoticesAScreenListedTheOtherWayRoundOrAScenarioThatStopsShort() {
		val first = surfaces.first { it.size > 1 }
		val reversed = scenarioWaiting("coachmarks-reversed", first.reversed())
		val partial = scenarioWaiting("coachmarks-partial", first.dropLast(1))
		val interleaved = scenarioWaiting("coachmarks-interleaved", listOf(first.first(), CoachmarkId.About, first.last()))
		val exact = scenarioWaiting("coachmarks-exact", first)
		val other = scenario("other-scenario", "x", clean) {
			first.forEach { waitVisible(CoachmarkUiTags.currentCoachmark(it), Within.Action) }
		}

		assertFalse(showsInOrder(awaitedBy(listOf(reversed, partial, interleaved, other)), first))
		assertTrue(showsInOrder(awaitedBy(listOf(reversed, exact)), first))
	}

	@Test
	fun aNewScreenReusingAListOrListingAnotherCoachmarkChangesTheCounts() {
		assertTrue(listingMismatches(surfaces + listOf(surfaces.first())).isNotEmpty(), "a screen that reuses a list")
		assertTrue(listingMismatches(surfaces + listOf(listOf(CoachmarkId.Summary))).isNotEmpty(), "a second summary")
		assertTrue(listingMismatches(surfaces.drop(1)).isNotEmpty(), "a screen that stopped listing its coachmarks")
	}

	/** `coachmark: found n, expected m` for every coachmark whose listings differ from what is expected. */
	private fun listingMismatches(listed: List<List<CoachmarkId>>): List<String> {
		val counts = listed.flatten().groupingBy { it }.eachCount()

		return CoachmarkId.entries.mapNotNull { id ->
			val expected = if (id in listedTwice) 2 else 1

			"${id.name}: found ${counts[id] ?: 0}, expected $expected".takeIf { (counts[id] ?: 0) != expected }
		}
	}

	/** The coachmark ids each `coachmarks-*` scenario of [scenarios] waits for, in the order it waits. */
	private fun awaitedBy(scenarios: List<Scenario>): List<List<String>> =
		scenarios.filter { it.id.startsWith("coachmarks-") }.map { scenario ->
			scenario.steps.flattened().filterIsInstance<Step.WaitVisible>().mapNotNull { (it.q as? Query.Tag)?.value }
		}

	private fun showsInOrder(awaited: List<List<String>>, surface: List<CoachmarkId>): Boolean {
		val expected = surface.map { CoachmarkUiTags.currentCoachmark(it) }

		return awaited.any { waits -> waits.windowed(expected.size).any { it == expected } }
	}

	private fun scenarioWaiting(id: String, ids: List<CoachmarkId>): Scenario =
		scenario(id, "coachmarks", clean) {
			ids.forEach { waitVisible(CoachmarkUiTags.currentCoachmark(it), Within.Action) }
		}
}
