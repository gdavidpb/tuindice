package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.unavailableState

internal fun MachineDefinitionBuilder<SummaryStatistics.State>.statisticsLoadingTransitions() {
	from<SummaryStatistics.State.Loading> {
		onTo<SummaryStatisticsInternalEvent.StatisticsObserved, SummaryStatistics.State.Content> { _, event ->
			SummaryStatistics.State.Content(statistics = event.statistics)
		}

		onTo<SummaryStatisticsInternalEvent.StatisticsUnavailable, SummaryStatistics.State.Unavailable> { _, _ ->
			unavailableState()
		}
	}
}
