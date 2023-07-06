package com.gdavidpb.tuindice.evaluations.utils

import kotlin.time.Duration.Companion.days

const val MIN_EVALUATION_GRADE = 0.0
const val MAX_EVALUATION_GRADE = 100.0

object PreferencesKeys {
	const val COOLDOWN_GET_EVALUATIONS = "cooldownGetEvaluations"
}

object CooldownTimes {
	val COOLDOWN_GET_EVALUATIONS = 1.days.inWholeMilliseconds
}