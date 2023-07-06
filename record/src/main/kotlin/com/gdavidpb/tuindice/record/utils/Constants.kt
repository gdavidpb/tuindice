package com.gdavidpb.tuindice.record.utils

import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import kotlin.time.Duration.Companion.days

object Ranges {
	val subjectGrade = MIN_SUBJECT_GRADE.toFloat()..MAX_SUBJECT_GRADE.toFloat()
}

object PreferencesKeys {
	const val COOLDOWN_GET_QUARTERS = "cooldownGetQuarters"
}

object CooldownTimes {
	val COOLDOWN_GET_QUARTERS = 1.days.inWholeMilliseconds
}