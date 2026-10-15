package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Materias": the closed terms' subjects by outcome, as one stacked bar. The bar holds the subjects with a result,
 * so its whole is their sum, while the figure counts every subject. [facts] are the compact figures under the legend:
 * the approvals on the first attempt, the repeated subjects, and the subjects without a result when there are any.
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

		/** [chartSummary] is what a screen reader hears of the bar: the figure and what it counts, as one phrase. */
		data class Data(
			val figure: SummaryStatisticsFigureItem,
			val segments: List<SummaryStatisticsSegmentItem>,
			val facts: List<SummaryStatisticsFactItem>,
			val chartSummary: UiText
		) : Content
	}
}
