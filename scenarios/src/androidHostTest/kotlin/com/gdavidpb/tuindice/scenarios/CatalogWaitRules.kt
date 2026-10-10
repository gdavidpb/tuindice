package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import com.gdavidpb.tuindice.scenariokit.model.describe

/**
 * The rules about waits for something to be gone, as functions that return what breaks them, so
 * `CatalogWaitRulesTest` runs each on the catalog (nothing breaks it) and on a scenario made to break it. A wait for
 * something to be gone passes at once when it was never there, so each rule asks for the evidence that it was.
 */
internal object CatalogWaitRules {
	private val snackbar = Query.Tag(BaseUiTags.SnackbarContainer)

	/**
	 * `scenario (platform): element` for every `waitGone` of an element of the system that nothing in the scenario made
	 * visible or tapped before, on the same platform branch (YE-1). The branch counts: a `waitVisible` inside the branch
	 * of the other platform proves nothing here. Without that evidence a gesture that opened nothing passes, because
	 * the wait after it is already true.
	 */
	fun systemElementsGoneWithoutHavingBeenSeen(scenarios: List<Scenario>): List<String> =
		scenarios.flatMap { scenario ->
			val steps = scenario.steps.located()

			steps.mapIndexedNotNull { index, located ->
				val query = (located.step as? Step.WaitGone)?.q as? Query.System

				query?.takeUnless { steps.take(index).any { earlier -> showsOrTouches(earlier, query, located.platform) } }
					?.let { "${scenario.id} (${located.platform?.name ?: "every platform"}): ${it.describe()}" }
			}
		}

	/**
	 * `scenario: step` for every `waitGone` of the snackbar in a scenario that never waited for one to be visible
	 * before, unless it takes only the window of `Timeouts.Probe` and comes right after a `waitVisible` (YE-2). A
	 * snackbar without an action lasts 4 s, longer than the probe, so one shown together with what the previous step
	 * waited for is still there when the window ends and the step fails; a longer wait would see it go and pass. It is
	 * a window and not a proof of absence.
	 */
	fun snackbarAbsencesNotReadRightAfterTheirCause(scenarios: List<Scenario>): List<String> =
		scenarios.flatMap { scenario ->
			val steps = scenario.steps.flattened()

			steps.mapIndexedNotNull { index, step ->
				val gone = step as? Step.WaitGone
				val seenBefore = steps.take(index).any { it is Step.WaitVisible && it.q == snackbar }
				val readRightAfter = gone?.timeoutMs == Timeouts.Probe && steps.getOrNull(index - 1) is Step.WaitVisible

				"${scenario.id}: ${step.target}".takeIf { gone?.q == snackbar && !seenBefore && !readRightAfter }
			}
		}

	private fun showsOrTouches(earlier: LocatedStep, query: Query, platform: Platform?) =
		(earlier.platform == null || earlier.platform == platform) &&
			((earlier.step as? Step.WaitVisible)?.q == query || (earlier.step as? Step.Tap)?.q == query)
}
