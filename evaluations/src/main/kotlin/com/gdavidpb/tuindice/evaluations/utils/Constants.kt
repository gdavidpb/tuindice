package com.gdavidpb.tuindice.evaluations.utils

import kotlin.time.Duration.Companion.days

const val THRESHOLD_EVALUATION_SWIPE = .3f

object PreferencesKeys {
	const val COOLDOWN_GET_EVALUATIONS = "cooldownGetEvaluations"
}

object CooldownTimes {
	val COOLDOWN_GET_EVALUATIONS = 1.days.inWholeMilliseconds
}