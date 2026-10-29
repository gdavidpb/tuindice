package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine

internal fun MachineDefinitionBuilder<TermReport.State>.termReportNoReportTransitions(
	machine: TermReportMachine,
	host: MachineHost<TermReport.Effect>
) {
	from<TermReport.State.NoReport> {
		// The report arrives after all: the screen shows it.
		onTo<TermReportInternalEvent.ReportObserved, TermReport.State.Content> { _, event ->
			machine.contentOf(host = host, report = event.report)
		}

		on<TermReportInternalEvent.ReportMissing> { state, _ -> state }

		// A cause that keeps the report from being read says nothing on a screen that already says there is no report.
		on<TermReportInternalEvent.ReportUnavailable> { state, _ -> state }

		// A refusal says nothing on a screen that already says there is no report: no thumb is shown to take back.
		on<TermReportInternalEvent.FeedbackRejected> { state, _ -> state }

		// The end of a vote says nothing on a screen that already says there is no report either.
		on<TermReportInternalEvent.ThumbSettled> { state, _ -> state }
	}
}
