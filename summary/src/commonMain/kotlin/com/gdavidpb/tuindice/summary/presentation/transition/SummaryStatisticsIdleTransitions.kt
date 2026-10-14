package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsMachine

internal fun MachineDefinitionBuilder<SummaryStatistics.State>.statisticsIdleTransitions(
	machine: SummaryStatisticsMachine,
	host: MachineHost<SummaryStatistics.Effect>
) {
	from<SummaryStatistics.State.Idle> {
		onTo<SummaryStatistics.Action.ObserveStatistics, SummaryStatistics.State.Loading> { _, _ ->
			machine.startObservation(host = host)
			SummaryStatistics.State.Loading
		}
	}
}
