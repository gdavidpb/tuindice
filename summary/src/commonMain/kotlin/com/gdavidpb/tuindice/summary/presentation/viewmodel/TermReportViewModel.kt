package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem

class TermReportViewModel(
	override val screenMachine: TermReportMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<TermReport.State, TermReport.Action, TermReport.Effect>(
	name = "summary_term_report",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun openAction(insightId: String) =
		sendAction(TermReport.Action.Open(insightId = insightId))

	fun retryAction() =
		sendAction(TermReport.Action.Retry)

	fun rateAction(useful: Boolean) =
		sendAction(TermReport.Action.Rate(useful = useful))

	fun shareAction() =
		sendAction(TermReport.Action.Share)

	fun openNextAction(action: TermReportActionItem) =
		sendAction(TermReport.Action.OpenNextAction(action = action))
}
