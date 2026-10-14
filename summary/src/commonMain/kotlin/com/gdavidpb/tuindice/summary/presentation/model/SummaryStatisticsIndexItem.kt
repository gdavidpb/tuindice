package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Índice": the cumulative average the closed terms built, as a line, with the index of each term as its marker. [best]
 * and [worst] name the regular terms with the highest and the lowest index, the worst only when there are two or more.
 */
@Immutable
data class SummaryStatisticsIndexItem(
	val title: UiText,
	val content: Content
) {
	sealed interface Content {
		data class Empty(
			val text: UiText
		) : Content

		/**
		 * [figure] is the cumulative average at the last closed term, which [caption] names. The chart runs from
		 * [minValue] to [maxValue] on its scale.
		 */
		data class Data(
			val figure: SummaryStatisticsFigureItem,
			val caption: UiText,
			val points: List<SummaryStatisticsLinePointItem>,
			val minValue: Float,
			val maxValue: Float,
			val best: UiText?,
			val worst: UiText?
		) : Content
	}
}
