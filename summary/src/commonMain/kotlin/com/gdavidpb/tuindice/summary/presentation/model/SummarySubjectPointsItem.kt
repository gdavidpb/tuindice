package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A subject of the term and its points: [valueText] ("41") and [restText] ("de 60 puntos", lighter)
 * when it has graded evaluations, or [restText] alone ("Sin notas aún") when none has.
 */
@Immutable
data class SummarySubjectPointsItem(
	val subjectCode: String,
	val valueText: String?,
	val restText: UiText
)
