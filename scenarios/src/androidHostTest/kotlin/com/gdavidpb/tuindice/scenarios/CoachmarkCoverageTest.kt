package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every coachmark of every screen is shown, in the order the screen lists them, by a `coachmarks-*` scenario. The
 * screens and their order are read from the product (`CoachmarkSurface.kt`), not written here, so a coachmark
 * added to a screen, or moved within it, fails this test until a scenario follows it.
 */
class CoachmarkCoverageTest {
	private val surfaceSource =
		RepoFiles.file("wizard/src/commonMain/kotlin/com/gdavidpb/tuindice/wizard/presentation/mapper/CoachmarkSurface.kt")

	/** Each `listOf(CoachmarkId.A, CoachmarkId.B)` of the source: the coachmarks one screen lists, in order. */
	private val surfaces: List<List<CoachmarkId>> = Regex("""listOf\(([^)]*CoachmarkId\.[^)]*)\)""")
		.findAll(surfaceSource.readText())
		.map { match ->
			Regex("""CoachmarkId\.(\w+)""").findAll(match.groupValues[1]).map { CoachmarkId.valueOf(it.groupValues[1]) }.toList()
		}
		.distinct()
		.toList()

	/** The coachmark ids each `coachmarks-*` scenario waits for, in the order it waits. */
	private val awaited: Map<String, List<String>> = E2eCatalog.all.filter { it.id.startsWith("coachmarks-") }
		.associate { scenario ->
			scenario.id to scenario.steps.flattened().filterIsInstance<Step.WaitVisible>()
				.mapNotNull { (it.q as? Query.Tag)?.value }
		}

	@Test
	fun theSurfacesReadFromTheProductListEveryCoachmarkOnce() {
		assertEquals(
			CoachmarkId.entries.toSet(),
			surfaces.flatten().toSet(),
			"the parser must find every CoachmarkId in CoachmarkSurface.kt"
		)
		assertEquals(CoachmarkId.entries.size, surfaces.sumOf { it.size }, "a coachmark is listed by one screen only")
	}

	@Test
	fun everyScreenHasAScenarioThatShowsItsCoachmarksInOrder() {
		surfaces.forEach { surface ->
			val expected = surface.map { CoachmarkUiTags.currentCoachmark(it) }

			assertTrue(
				awaited.values.any { contains(it, expected) },
				"no coachmarks-* scenario waits for ${surface.map { it.name }} in that order"
			)
		}
	}

	@Test
	fun theOrderCheckNoticesAScreenListedTheOtherWayRound() {
		val reversed = surfaces.first { it.size > 1 }.reversed().map { CoachmarkUiTags.currentCoachmark(it) }

		assertTrue(awaited.values.none { contains(it, reversed) }, "the reversed order should not be found")
	}

	private fun contains(waits: List<String>, sequence: List<String>): Boolean =
		waits.windowed(sequence.size).any { it == sequence }
}
