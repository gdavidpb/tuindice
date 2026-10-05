package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection

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

private val whitespace = Regex("\\s+")

// Written in capitals whatever the sentence: the names the university goes by.
private val acronyms = setOf("DACE", "DST", "USB", "UC")

// Lowercases the words but not what must stay in capitals: a subject code ("MA1111") and the
// acronyms above.
internal fun String.toSentenceCase(): String {
	return trim()
		.split(whitespace)
		.joinToString(separator = " ") { word ->
			val core = word.filter(Char::isLetterOrDigit).uppercase()

			if (core.any(Char::isDigit) || core in acronyms) word.uppercase() else word.lowercase()
		}
		.replaceFirstChar { first -> first.uppercase() }
}
