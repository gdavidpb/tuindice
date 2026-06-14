package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent

internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsFailedTransitions() {
	from<Evaluations.State.Failed> {
		// Keep-current-while-waiting: an unsynced or record-waiting observation keeps
		// the failure visible instead of flashing back to Loading.
		on<EvaluationsInternalEvent.EvaluationsWaitingObserved> { state, _ -> state }
	}
}
