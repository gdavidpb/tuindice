package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Materias": the closed terms' subjects by outcome, as one stacked bar. The bar holds the subjects with a result,
 * so its whole is their sum, while the figure counts every subject. [notes] are the lines under the bar: the approvals
 * on the first attempt, and the repeated subjects. [noResult] counts the subjects without a result, as a line of its
 * own, or is null when there are none.
 */
@Immutable
data class SummaryStatisticsSubjectsItem(
	val title: UiText,
	val content: Content
) {
	sealed interface Content {
		data class Empty(
			val text: UiText
		) : Content

		data class Data(
			val figure: SummaryStatisticsFigureItem,
			val segments: List<SummaryStatisticsSegmentItem>,
			val notes: List<UiText>,
			val noResult: UiText?
		) : Content
	}
}
