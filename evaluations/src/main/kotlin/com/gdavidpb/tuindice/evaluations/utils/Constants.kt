package com.gdavidpb.tuindice.evaluations.utils

import kotlin.time.Duration.Companion.days

const val MIN_EVALUATION_GRADE = 0.25
const val MAX_EVALUATION_GRADE = 100.0

const val DECIMALS_GRADE_SUBJECT = 2

object PreferencesKeys {
	const val COOLDOWN_GET_EVALUATIONS = "cooldownGetEvaluations"
}

object CooldownTimes {
	val COOLDOWN_GET_EVALUATIONS = 1.days.inWholeMilliseconds
}