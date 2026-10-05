package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent

internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsRecordDataUnavailableTransitions() {
	from<Evaluations.State.RecordDataUnavailable> {
		// The record is what is missing, and fetching the evaluations does not bring it: the refresh
		// that every visit starts must not swap this explanation for a spinner nothing would ever
		// replace (the observation only speaks again when the record data changes).
		on<EvaluationsInternalEvent.EvaluationsRefreshStarted> { state, _ -> state }
	}
}
