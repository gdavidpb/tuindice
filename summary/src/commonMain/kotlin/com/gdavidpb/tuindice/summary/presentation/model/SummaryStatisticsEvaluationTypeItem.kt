package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * One evaluation type of "Este trimestre": its [name], how many of its evaluations are graded, its [scoreText] (the
 * weighted performance, or "Sin notas aún") and the [fraction] of the bar. [tooltip] is null while none is graded,
 * since then the bar has nothing to point at and it is not tappable.
 */
@Immutable
data class SummaryStatisticsEvaluationTypeItem(
	val name: UiText,
	val gradedText: UiText,
	val scoreText: UiText,
	val fraction: Float,
	val tooltip: SummaryStatisticsTooltipItem?
)
