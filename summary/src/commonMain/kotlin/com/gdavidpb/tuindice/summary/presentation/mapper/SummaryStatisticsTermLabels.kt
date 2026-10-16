package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic

private const val MONTH_SEPARATOR = " - "
private const val ONE_LETTER = 1
private const val THREE_LETTERS = 3
private const val CENTURY = 100
private const val YEAR_DIGITS = 2

// The name of each term on a chart axis: the first letter of each month of its period and the last two digits of its
// year, as in "S-D 21". When two terms of the record would be named the same, both keep their three-letter months
// instead, as in "Ene-Mar 24" and "Ene-May 24". The result is keyed by the term's key.
internal fun axisLabelsOf(terms: List<TermStatistic>): Map<String, String> {
	val shortNames = terms.associate { term -> term.termKey to term.axisName(threeLetters = false) }
	val repeated = shortNames.values
		.groupingBy { name -> name }
		.eachCount()
		.filterValues { count -> count > 1 }
		.keys

	return terms.associate { term ->
		val shortName = shortNames.getValue(term.termKey)
		val name = if (shortName in repeated) term.axisName(threeLetters = true) else shortName

		term.termKey to name
	}
}

// The whole name of a term, as the screen names it: "Ene - Mar 2024".
internal fun TermStatistic.fullLabel(): String = "${periodCode.shortLabel} $periodYear"

private fun TermStatistic.axisName(threeLetters: Boolean): String {
	val letterCount = if (threeLetters) THREE_LETTERS else ONE_LETTER
	val months = periodCode.shortLabel
		.split(MONTH_SEPARATOR)
		.joinToString(separator = "-") { month -> month.take(letterCount) }
	val year = (periodYear % CENTURY).toString().padStart(YEAR_DIGITS, '0')

	return "$months $year"
}
