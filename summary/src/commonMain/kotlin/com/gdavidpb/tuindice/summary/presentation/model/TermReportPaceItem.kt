package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/** The pace of the regular terms: the bars under their average, or the words that say why there is no pace yet. */
@Immutable
sealed interface TermReportPaceItem {
	data class Empty(
		val text: UiText
	) : TermReportPaceItem

	data class Data(
		val bars: List<SummaryStatisticsBarItem>,
		val reference: SummaryStatisticsReferenceItem,
		val currentLegend: UiText,
		val averageLegend: UiText
	) : TermReportPaceItem
}
