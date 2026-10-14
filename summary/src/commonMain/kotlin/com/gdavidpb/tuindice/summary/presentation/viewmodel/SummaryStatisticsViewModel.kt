package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsMachine
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget

class SummaryStatisticsViewModel(
	override val screenMachine: SummaryStatisticsMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SummaryStatistics.State, SummaryStatistics.Action, SummaryStatistics.Effect>(
	name = "summary_statistics",
	initialState = screenMachine.initialState(),
	initialAction = SummaryStatistics.Action.ObserveStatistics,
	dispatchers = dispatchers
) {
	fun resumedAction() =
		sendAction(SummaryStatistics.Action.Resumed)

	fun openTargetAction(target: SummaryHubTarget) =
		sendAction(SummaryStatistics.Action.OpenTarget(target))
}
