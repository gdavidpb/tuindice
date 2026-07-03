package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey

private const val STORAGE_VALUE_CONTINUOUS = "continuous"
private const val STORAGE_PREFIX_ACADEMIC = "academic:"

fun EvaluationsWeekKey.toStorageValue(): String = when (this) {
	EvaluationsWeekKey.Continuous -> STORAGE_VALUE_CONTINUOUS
	is EvaluationsWeekKey.Academic -> "$STORAGE_PREFIX_ACADEMIC$weekNumber"
}

fun String.toEvaluationsWeekKeyOrNull(): EvaluationsWeekKey? = when {
	this == STORAGE_VALUE_CONTINUOUS -> EvaluationsWeekKey.Continuous

	startsWith(STORAGE_PREFIX_ACADEMIC) ->
		removePrefix(STORAGE_PREFIX_ACADEMIC)
			.toIntOrNull()
			?.let(EvaluationsWeekKey::Academic)

	else -> null
}
