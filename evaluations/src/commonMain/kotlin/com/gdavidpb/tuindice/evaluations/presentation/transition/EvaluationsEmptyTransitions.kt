package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent

internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsEmptyTransitions() {
	from<Evaluations.State.Empty> {
		on<EvaluationsInternalEvent.EvaluationsObservationFailed> { state, _ -> state }

		on<EvaluationsInternalEvent.EvaluationsRefreshStarted> { state, _ -> state }

		on<EvaluationsInternalEvent.EvaluationsRefreshFailed> { state, _ -> state }
	}
}
