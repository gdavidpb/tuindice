package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.swipeFrom
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import kotlin.time.Duration.Companion.milliseconds

private const val SHEET_SWIPE_MS = 600L

/** Closes the Schedule sheet the way a user does, dragging it down by its title, and waits for it to be gone. */
fun StepBuilder.closeScheduleSheet() {
	swipeFrom(RecordUiTags.ScheduleTitle, SwipeDirection.Down, SHEET_SWIPE_MS.milliseconds)
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)
}
