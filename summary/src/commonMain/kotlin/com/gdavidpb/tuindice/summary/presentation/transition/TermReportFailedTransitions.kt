package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.presentation.mapper.termReportMissingState
import com.gdavidpb.tuindice.summary.presentation.mapper.termReportUnavailableState

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

		// The report can be read after all, whatever the cause that kept it: the screen shows it.
		onTo<TermReportInternalEvent.ReportObserved, TermReport.State.Content> { _, event ->
			machine.contentOf(host = host, report = event.report)
		}

		onTo<TermReportInternalEvent.ReportMissing, TermReport.State.NoReport> { _, _ ->
			termReportMissingState()
		}

		// The cause can change while the screen waits: the words say the cause the observation reports now.
		on<TermReportInternalEvent.ReportUnavailable> { _, event ->
			termReportUnavailableState(cause = event.cause)
		}
	}
}
