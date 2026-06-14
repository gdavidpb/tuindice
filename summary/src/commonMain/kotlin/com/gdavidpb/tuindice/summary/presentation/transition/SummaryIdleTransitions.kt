package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine

internal fun MachineDefinitionBuilder<Summary.State>.idleTransitions(
	machine: SummaryMachine,
	host: MachineHost<Summary.Effect>
) {
	from<Summary.State.Idle> {
		onTo<Summary.Action.RefreshSummary, Summary.State.Loading> { _, _ ->
			machine.refreshUser(host = host)
			Summary.State.Loading(isUserRefreshing = true)
		}

		on<SummaryInternalEvent.RefreshSucceeded> { state, _ -> state }
	}
}
