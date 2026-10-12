package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.LocalDate

private const val STORAGE_VALUE_CONTINUOUS = "continuous"
private const val STORAGE_PREFIX_ACADEMIC = "academic:"
private const val STORAGE_SEPARATOR = ":"
private const val ACADEMIC_PARTS = 2

/**
 * The text the selected week is saved as: `continuous`, or `academic:<anchor>:<week>` where
 * `<anchor>` is the Monday that opened week 1 when the choice was made, in ISO (for example
 * `academic:2026-09-28:7`). The week number alone would point at other dates once the anchor moves.
 * An academic week cannot be saved without its anchor: null then.
 */
fun EvaluationsWeekKey.toStorageValue(weekOneStart: LocalDate?): String? = when (this) {
	EvaluationsWeekKey.Continuous -> STORAGE_VALUE_CONTINUOUS

	is EvaluationsWeekKey.Academic ->
		weekOneStart?.let { anchor -> "$STORAGE_PREFIX_ACADEMIC$anchor$STORAGE_SEPARATOR$weekNumber" }
}

/**
 * The week a saved text stands for under the current [weekOneStart], or null when it stands for none:
 * an unknown text, a saved week of another anchor (its number would mean other dates now) or one in
 * the old format without an anchor (`academic:<week>`). `continuous` does not depend on the anchor.
 */
fun String.toEvaluationsWeekKeyOrNull(weekOneStart: LocalDate): EvaluationsWeekKey? = when {
	this == STORAGE_VALUE_CONTINUOUS -> EvaluationsWeekKey.Continuous

	startsWith(STORAGE_PREFIX_ACADEMIC) -> toAcademicWeekKeyOrNull(weekOneStart)

	else -> null
}

private fun String.toAcademicWeekKeyOrNull(weekOneStart: LocalDate): EvaluationsWeekKey? {
	val parts = removePrefix(STORAGE_PREFIX_ACADEMIC).split(STORAGE_SEPARATOR)

	if (parts.size != ACADEMIC_PARTS) return null

	val anchor = runCatching { LocalDate.parse(parts[0]) }.getOrNull()
	val weekNumber = parts[1].toIntOrNull()

	return if (anchor == weekOneStart && weekNumber != null) {
		EvaluationsWeekKey.Academic(weekNumber)
	} else {
		null
	}
}
