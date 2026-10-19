package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport

internal fun MachineDefinitionBuilder<TermReport.State>.termReportAnyStateTransitions() {
	fromAny {
		// The screen asks for its report once it opens. Asked again once it is open, the same report is already on screen:
		// the ask changes nothing, and no transition is reported as invalid for it. Idle keeps its own row for it.
		on<TermReport.Action.Open> { state, _ -> state }
	}
}
