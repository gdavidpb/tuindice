package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.presentation.machine.termReportMissingState

internal fun MachineDefinitionBuilder<TermReport.State>.termReportFailedTransitions(
	machine: TermReportMachine,
	host: MachineHost<TermReport.Effect>
) {
	from<TermReport.State.Failed> {
		// The student asks again: the report is read once more.
		onTo<TermReport.Action.Retry, TermReport.State.Loading> { _, _ ->
			machine.retry(host = host)

			TermReport.State.Loading
		}

		onTo<TermReportInternalEvent.ReportObserved, TermReport.State.Content> { _, event ->
			TermReport.State.Content(report = event.report)
		}

		onTo<TermReportInternalEvent.ReportMissing, TermReport.State.NoReport> { _, _ ->
			termReportMissingState()
		}

		on<TermReportInternalEvent.ReportUnavailable> { state, _ -> state }
	}
}
