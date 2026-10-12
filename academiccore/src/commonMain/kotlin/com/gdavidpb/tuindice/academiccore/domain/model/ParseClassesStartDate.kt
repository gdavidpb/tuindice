package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.datetime.LocalDate

/** Reads a backend `classes_start_date` (ISO yyyy-MM-dd): null when absent or malformed. */
fun parseClassesStartDate(value: String?): LocalDate? {
	if (value == null) return null

	return try {
		LocalDate.parse(value)
	} catch (_: IllegalArgumentException) {
		null
	}
}
