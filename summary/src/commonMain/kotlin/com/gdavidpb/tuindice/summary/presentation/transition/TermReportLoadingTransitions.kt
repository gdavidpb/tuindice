package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.termReportMissingState
import com.gdavidpb.tuindice.summary.presentation.machine.termReportUnavailableState

internal fun MachineDefinitionBuilder<TermReport.State>.termReportLoadingTransitions() {
	from<TermReport.State.Loading> {
		onTo<TermReportInternalEvent.ReportObserved, TermReport.State.Content> { _, event ->
			TermReport.State.Content(report = event.report)
		}

		onTo<TermReportInternalEvent.ReportMissing, TermReport.State.NoReport> { _, _ ->
			termReportMissingState()
		}

		onTo<TermReportInternalEvent.ReportUnavailable, TermReport.State.Failed> { _, _ ->
			termReportUnavailableState()
		}
	}
}
