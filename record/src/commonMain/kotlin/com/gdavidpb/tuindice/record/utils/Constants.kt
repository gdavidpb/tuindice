package com.gdavidpb.tuindice.record.utils

import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.MIN_SUBJECT_GRADE
import kotlin.time.Duration.Companion.days

object Ranges {
	val subjectGrade = MIN_SUBJECT_GRADE.toFloat()..MAX_SUBJECT_GRADE.toFloat()
}

object PreferencesKeys {
	const val COOLDOWN_GET_QUARTERS = "cooldownGetQuarters"
	const val COOLDOWN_GET_RECORD = "cooldownGetRecord"
	const val SELECTED_HISTORICAL_TERM_ID = "selectedHistoricalTermId"
	const val SELECTED_PROJECTION_TERM_ID = "selectedProjectionTermId"
	const val RECORD_VIEW_MODE = "recordViewMode"
}

object CooldownTimes {
	val COOLDOWN_GET_RECORD = 1.days.inWholeMilliseconds
}
