package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryInternalEvent

// The line of the term reports, which the summary reads apart from its hub. The hub is a field of every state, so a
// reading of it keeps the line it has, and a reading of the line keeps the hub it has.
internal fun MachineDefinitionBuilder<Summary.State>.summaryTermReportTransitions() {
	fromAny {
		on<SummaryInternalEvent.HubObserved> { state, event ->
			state.withHub(hub = event.hub.copy(termReport = state.hub.termReport))
		}

		on<SummaryInternalEvent.TermReportEntryObserved> { state, event ->
			state.withHub(hub = state.hub.copy(termReport = event.entry))
		}
	}
}
