package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent

internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsLoadingTransitions() {
	from<Evaluations.State.Loading> {
		on<EvaluationsInternalEvent.EvaluationsEmptyObserved> { state, _ -> state }
	}
}
