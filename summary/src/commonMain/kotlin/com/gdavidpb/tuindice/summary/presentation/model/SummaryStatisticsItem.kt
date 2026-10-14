package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * What the Statistics screen shows, for the moment it was read: the six blocks, in the order the screen lists them,
 * each with its data or with the text that says what it lacks. [chartActionLabel] is what tapping a point of any chart
 * offers to a screen reader.
 */
@Immutable
data class SummaryStatisticsItem(
	val currentTerm: SummaryStatisticsCurrentTermItem,
	val index: SummaryStatisticsIndexItem,
	val subjects: SummaryStatisticsSubjectsItem,
	val credits: SummaryStatisticsCreditsItem,
	val grades: SummaryStatisticsGradesItem,
	val pace: SummaryStatisticsPaceItem,
	val chartActionLabel: UiText
)
