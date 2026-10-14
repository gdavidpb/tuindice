package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsMachine

internal fun MachineDefinitionBuilder<SummaryStatistics.State>.statisticsAnyStateTransitions(
	machine: SummaryStatisticsMachine,
	host: MachineHost<SummaryStatistics.Effect>
) {
	fromAny {
		// The screen is back in front: the hub is observed again, so the clock is read again.
		on<SummaryStatistics.Action.Resumed> { state, _ ->
			machine.resumed(host = host)
			state
		}
	}
}
