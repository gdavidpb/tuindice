package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine

internal fun MachineDefinitionBuilder<TermReport.State>.termReportIdleTransitions(
	machine: TermReportMachine,
	host: MachineHost<TermReport.Effect>
) {
	from<TermReport.State.Idle> {
		onTo<TermReport.Action.Open, TermReport.State.Loading> { _, action ->
			machine.open(host = host, insightId = action.insightId)

			TermReport.State.Loading
		}
	}
}
