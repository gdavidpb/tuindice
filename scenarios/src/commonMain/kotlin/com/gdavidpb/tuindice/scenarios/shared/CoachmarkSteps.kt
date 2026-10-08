package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags

/**
 * The coachmarks one screen lists, in the order `eligibleCoachmarkIds` gives them: each is awaited, confirmed, and the
 * next one is awaited only after the previous one has left, so a bubble can never answer for another. After the
 * last one the bubble is gone and the screen underneath can be touched.
 */
fun StepBuilder.confirmCoachmarks(vararg ids: CoachmarkId) {
	ids.forEachIndexed { index, id ->
		waitVisible(CoachmarkUiTags.currentCoachmark(id), Within.Action)
		if (index > 0) waitGone(CoachmarkUiTags.currentCoachmark(ids[index - 1]), Within.Assert)
		tap(CoachmarkUiTags.ConfirmButton)
	}
	waitGone(CoachmarkUiTags.Bubble, Within.Action)
}
