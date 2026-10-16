package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A subject of the term and its points: [valueText] ("41") and [restText] ("de 60 puntos", lighter) when it has
 * graded evaluations; [restText] alone is "Sin notas aún" when it has evaluations but none graded, and
 * "Sin evaluaciones" when it has none.
 *
 * [evaluatedText] says what it has evaluated of its points ("10 de 100 evaluados") once a grade is in. The row does not
 * draw it: the bar says it to a screen reader, with the rest of the row. It is absent while none is graded, since
 * "Sin notas aún" already says that nothing is evaluated.
 *
 * The bar is two shares of its points, both between nothing and all: [earnedFraction], what it has earned, drawn first
 * in the subject's colour, and [evaluatedFraction], what it has evaluated, whose part past the earned one is drawn
 * lighter. The earned share is never more than the evaluated one. A subject with no evaluations has no bar, so
 * [evaluatedFraction] is absent for it, and [earnedFraction] is zero.
 *
 * [nextText] names its next evaluation without a grade ("Próx.: Parcial 2 · lun 26"), absent when there is none.
 * [attemptId] is the attempt of the subject in Record, where the row leads.
 */
@Immutable
data class SummarySubjectPointsItem(
	val subjectCode: String,
	val valueText: String?,
	val restText: UiText,
	val attemptId: String,
	val evaluatedText: UiText?,
	val earnedFraction: Float,
	val evaluatedFraction: Float?,
	val nextText: UiText?
)
