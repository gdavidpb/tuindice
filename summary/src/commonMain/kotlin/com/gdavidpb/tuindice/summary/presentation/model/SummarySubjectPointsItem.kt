package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A subject of the term and its points: [valueText] ("41") and [restText] ("de 60 puntos", lighter) when it has
 * graded evaluations; [restText] alone is "Sin notas aún" when it has evaluations but none graded, and
 * "Sin evaluaciones" when it has none. [evaluatedText] says what it has evaluated of its points ("10 de 100
 * evaluados") and [evaluatedFraction], between nothing and all, is the share of the bar that shows it; a subject with
 * no evaluations has no [evaluatedText], and its row is only its chip and "Sin evaluaciones". [nextText]
 * names its next evaluation without a grade ("Próx.: Parcial 2 · lun 26"), absent when there is none. [attemptId]
 * is the attempt of the subject in Record, where the row leads.
 */
@Immutable
data class SummarySubjectPointsItem(
	val subjectCode: String,
	val valueText: String?,
	val restText: UiText,
	val attemptId: String,
	val evaluatedText: UiText?,
	val evaluatedFraction: Float,
	val nextText: UiText?
)
