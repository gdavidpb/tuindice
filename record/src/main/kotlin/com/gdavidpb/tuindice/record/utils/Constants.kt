package com.gdavidpb.tuindice.record.utils

import kotlin.time.Duration.Companion.days

const val MIN_SUBJECT_GRADE = 0
const val MAX_SUBJECT_GRADE = 5

object Ranges {
	val subjectGrade = MIN_SUBJECT_GRADE.toFloat()..MAX_SUBJECT_GRADE.toFloat()
}

object PreferencesKeys {
	const val COOLDOWN_GET_QUARTERS = "cooldownGetQuarters"
}

object CooldownTimes {
	val COOLDOWN_GET_QUARTERS = 1.days.inWholeMilliseconds
}