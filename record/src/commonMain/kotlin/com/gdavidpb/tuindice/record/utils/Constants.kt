package com.gdavidpb.tuindice.record.utils

import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import kotlin.time.Duration.Companion.days

object Ranges {
	val subjectGrade = MIN_SUBJECT_GRADE.toFloat()..MAX_SUBJECT_GRADE.toFloat()
}

object PreferencesKeys {
	const val COOLDOWN_GET_QUARTERS = "cooldownGetQuarters"
	const val SELECTED_OFFICIAL_QUARTER_ID = "selectedOfficialQuarterId"
	const val SELECTED_WORKING_QUARTER_ID = "selectedWorkingQuarterId"
	const val COOLDOWN_GET_RECORD = "cooldownGetRecord"
	const val SELECTED_OFFICIAL_TERM_ID = "selectedOfficialTermId"
	const val SELECTED_WORKING_TERM_ID = "selectedWorkingTermId"
	const val RECORD_VIEW_MODE = "recordViewMode"
}

object CooldownTimes {
	val COOLDOWN_GET_QUARTERS = 1.days.inWholeMilliseconds
	val COOLDOWN_GET_RECORD = 1.days.inWholeMilliseconds
}
