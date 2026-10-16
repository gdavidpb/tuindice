package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.base.utils.extension.toSentenceCase

/**
 * "Sección 1 · MYS-116". The classroom is shown only when every meeting that names one agrees on
 * it; with several rooms only the section is shown, and with neither there is nothing to show.
 */
internal fun AttemptProjection.toEnrollmentDetailText(texts: RecordMapperTexts): String? {
	val classroom = sharedClassroom()
	val section = section ?: return classroom

	return if (classroom != null) {
		texts.termAttemptSectionClassroom(section, classroom)
	} else {
		texts.termAttemptSection(section)
	}
}

/** The one classroom every meeting that names a room agrees on, or null when there are several or none. */
internal fun AttemptProjection.sharedClassroom(): String? {
	return schedule
		.orEmpty()
		.map { entry -> entry.classroom.trim() }
		.filter { room -> room.isNotEmpty() }
		.distinct()
		.singleOrNull()
}

/**
 * The university prints its errors in capitals; the first one is shown in sentence case, followed
 * by how many more there are ("Choque de horario · +1").
 */
internal fun AttemptProjection.toEnrollmentErrorText(): String? {
	val errors = enrollmentErrors
		.orEmpty()
		.map { error -> error.trim() }
		.filter { error -> error.isNotEmpty() }
	val first = errors.firstOrNull()?.toSentenceCase() ?: return null

	return if (errors.size > 1) "$first · +${errors.size - 1}" else first
}
