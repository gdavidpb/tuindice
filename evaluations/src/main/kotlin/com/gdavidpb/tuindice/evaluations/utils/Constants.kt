package com.gdavidpb.tuindice.evaluations.utils

import kotlin.time.Duration.Companion.days

const val MIN_EVALUATION_GRADE = 0.0
const val MAX_EVALUATION_GRADE = 100.0

const val THRESHOLD_EVALUATION_SWIPE = .3f

object PreferencesKeys {
	const val COOLDOWN_GET_EVALUATIONS = "cooldownGetEvaluations"
}

object CooldownTimes {
	val COOLDOWN_GET_EVALUATIONS = 1.days.inWholeMilliseconds
}