package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine

internal fun MachineDefinitionBuilder<Summary.State>.loadingTransitions(
	machine: SummaryMachine,
	host: MachineHost<Summary.Effect>
) {
	from<Summary.State.Loading> {
		on<Summary.Action.RefreshSummary> { state, _ ->
			machine.refreshUser(host = host)
			state.copy(isUserRefreshing = true)
		}

		on<SummaryInternalEvent.RefreshSucceeded> { state, _ ->
			state.copy(isUserRefreshing = false)
		}
	}
}
