package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Materias": the subjects of the closed terms by outcome, as one stacked bar whose whole is every subject. [notes] are
 * the lines under the bar: the approvals on the first attempt, and the repeated subjects.
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
			val notes: List<UiText>
		) : Content
	}
}
