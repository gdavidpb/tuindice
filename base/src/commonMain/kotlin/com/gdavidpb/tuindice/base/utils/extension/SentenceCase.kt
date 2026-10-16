package com.gdavidpb.tuindice.base.utils.extension

private val whitespace = Regex("\\s+")

// Written in capitals whatever the sentence: the names the university goes by.
private val acronyms = setOf("DACE", "DST", "USB", "UC")

/**
 * The university prints its errors in capitals: "CHOQUE DE HORARIO" reads "Choque de horario". What
 * must stay in capitals (a subject code, which has a digit, and the acronyms the university goes by)
 * does. Runs of whitespace become one space and the ends are trimmed.
 */
fun String.toSentenceCase(): String {
	return trim()
		.split(whitespace)
		.joinToString(separator = " ") { word ->
			val core = word.filter(Char::isLetterOrDigit).uppercase()

			if (core.any(Char::isDigit) || core in acronyms) word.uppercase() else word.lowercase()
		}
		.replaceFirstChar { first -> first.uppercase() }
}
