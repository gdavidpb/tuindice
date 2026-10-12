package com.gdavidpb.tuindice.evaluations.utils

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

const val THRESHOLD_EVALUATION_SWIPE = .33f

object PreferencesKeys {
	const val COOLDOWN_GET_EVALUATIONS = "cooldownGetEvaluations"
	const val SELECTED_EVALUATIONS_WEEK = "selectedEvaluationsWeek"
}

object CooldownTimes {
	val COOLDOWN_GET_EVALUATIONS = 1.days.inWholeMilliseconds

	// After a failed download, the automatic revalidation that follows a sync waits this long; the
	// tab never waits.
	val COOLDOWN_RETRY_EVALUATIONS = 1.hours.inWholeMilliseconds
}

// A stamp in the future (a clock moved back, a hand-edited value) is outside every window: one
// extra request is cheaper than a revalidation that never runs again.
internal fun isWithinWindow(sinceMillis: Long, nowMillis: Long, windowMillis: Long): Boolean {
	return nowMillis - sinceMillis in 0 until windowMillis
}
