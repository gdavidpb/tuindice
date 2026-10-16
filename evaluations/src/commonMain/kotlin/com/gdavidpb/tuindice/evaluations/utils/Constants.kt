package com.gdavidpb.tuindice.evaluations.utils

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

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
