package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Créditos": the credits of the closed terms by outcome, as one stacked bar, each credit counted once by its subject.
 * The pensum's own progress comes after it, when a pensum is cached: [pensum] has its own figure and its track
 * [Pensum.fraction].
 */
@Immutable
data class SummaryStatisticsCreditsItem(
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
			val pensum: Pensum?
		) : Content
	}

	data class Pensum(
		val figure: SummaryStatisticsFigureItem,
		val fraction: Float
	)
}
