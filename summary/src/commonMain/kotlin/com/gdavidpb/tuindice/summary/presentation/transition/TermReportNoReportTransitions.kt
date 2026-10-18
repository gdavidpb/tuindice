package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent

internal fun MachineDefinitionBuilder<TermReport.State>.termReportNoReportTransitions() {
	from<TermReport.State.NoReport> {
		// The report arrives after all: the screen shows it.
		onTo<TermReportInternalEvent.ReportObserved, TermReport.State.Content> { _, event ->
			TermReport.State.Content(report = event.report)
		}

		on<TermReportInternalEvent.ReportMissing> { state, _ -> state }
	}
}
