package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsItem

/**
 * Internal machine inputs: the hub's observation said what the statistics are now, or that there are none to show. The
 * statistics arrive already built, since the observation owns the mapping.
 */
sealed interface SummaryStatisticsInternalEvent {
	data class StatisticsObserved(
		val statistics: SummaryStatisticsItem
	) : SummaryStatisticsInternalEvent

	data object StatisticsUnavailable : SummaryStatisticsInternalEvent
}
