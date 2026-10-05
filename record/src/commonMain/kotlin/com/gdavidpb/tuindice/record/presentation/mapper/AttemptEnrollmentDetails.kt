package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection

/**
 * "Sección 1 · MYS-116". The classroom is shown only when every meeting that names one agrees on
 * it; with several rooms only the section is shown, and with neither there is nothing to show.
 */
internal fun AttemptProjection.toEnrollmentDetailText(texts: RecordMapperTexts): String? {
	val classroom = schedule
		.orEmpty()
		.map { entry -> entry.classroom.trim() }
		.filter { room -> room.isNotEmpty() }
		.distinct()
		.singleOrNull()

	val section = section ?: return classroom

	return if (classroom != null) {
		texts.termAttemptSectionClassroom(section, classroom)
	} else {
		texts.termAttemptSection(section)
	}
}

/** The university prints its errors in capitals; the first one is shown in sentence case. */
internal fun AttemptProjection.toEnrollmentErrorText(): String? {
	return enrollmentErrors
		.orEmpty()
		.firstNotNullOfOrNull { error -> error.trim().takeIf { text -> text.isNotEmpty() } }
		?.toSentenceCase()
}

internal fun String.toSentenceCase(): String {
	val lower = lowercase()

	return lower.replaceFirstChar { first -> first.uppercase() }
}
