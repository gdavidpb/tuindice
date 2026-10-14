package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsInternalEvent

internal fun MachineDefinitionBuilder<SummaryStatistics.State>.statisticsUnavailableTransitions() {
	from<SummaryStatistics.State.Unavailable> {
		// The record is ready again: the statistics come back with the next observation.
		onTo<SummaryStatisticsInternalEvent.StatisticsObserved, SummaryStatistics.State.Content> { _, event ->
			SummaryStatistics.State.Content(statistics = event.statistics)
		}

		on<SummaryStatisticsInternalEvent.StatisticsUnavailable> { state, _ -> state }
	}
}
