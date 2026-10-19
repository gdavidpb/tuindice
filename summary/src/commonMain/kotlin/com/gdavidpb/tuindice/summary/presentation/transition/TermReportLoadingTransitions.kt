package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.presentation.mapper.termReportMissingState
import com.gdavidpb.tuindice.summary.presentation.mapper.termReportUnavailableState

internal fun MachineDefinitionBuilder<TermReport.State>.termReportLoadingTransitions(
	machine: TermReportMachine,
	host: MachineHost<TermReport.Effect>
) {
	from<TermReport.State.Loading> {
		onTo<TermReportInternalEvent.ReportObserved, TermReport.State.Content> { _, event ->
			machine.contentOf(host = host, report = event.report)
		}

		onTo<TermReportInternalEvent.ReportMissing, TermReport.State.NoReport> { _, _ ->
			termReportMissingState()
		}

		onTo<TermReportInternalEvent.ReportUnavailable, TermReport.State.Failed> { _, event ->
			termReportUnavailableState(cause = event.cause)
		}
	}
}
