package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.unavailableState

internal fun MachineDefinitionBuilder<SummaryStatistics.State>.statisticsContentTransitions(
	host: MachineHost<SummaryStatistics.Effect>
) {
	from<SummaryStatistics.State.Content> {
		// The hub says the statistics again, the minute after the last time: the screen is laid out with them.
		on<SummaryStatisticsInternalEvent.StatisticsObserved> { _, event ->
			SummaryStatistics.State.Content(statistics = event.statistics)
		}

		onTo<SummaryStatisticsInternalEvent.StatisticsUnavailable, SummaryStatistics.State.Unavailable> { _, _ ->
			unavailableState()
		}

		// Each link only asks to be taken to the screen that owns what it says: the screen stays as it is until whoever
		// hosts it answers.
		on<SummaryStatistics.Action.OpenTarget>(
			emits = setOf(SummaryStatistics.Effect.NavigateTo::class)
		) { state, action ->
			host.sendEffect(SummaryStatistics.Effect.NavigateTo(target = action.target))
			state
		}
	}
}
