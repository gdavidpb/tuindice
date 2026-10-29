package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.swipeFrom
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import kotlin.time.Duration.Companion.milliseconds

private const val SHEET_SWIPE_MS = 600L

/** Closes the Schedule sheet the way a user does, dragging it down by its title, and waits for it to be gone. */
fun StepBuilder.closeScheduleSheet() {
	swipeFrom(RecordUiTags.ScheduleTitle, SwipeDirection.Down, SHEET_SWIPE_MS.milliseconds)
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)
}

/** The schedule is the fourth place of the record's bar, so it waits in the ⋮ menu: opens the menu and taps it. */
fun StepBuilder.openScheduleSheetFromOverflow() {
	tap(BaseUiTags.TopAppBarOverflowButton)
	waitVisible(BaseUiTags.topBarOverflowItem(TopBarAction.RecordScheduleAction), Within.Action)
	tap(BaseUiTags.topBarOverflowItem(TopBarAction.RecordScheduleAction))
}
