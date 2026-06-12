package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine

internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsContentTransitions(
	machine: EvaluationsMachine,
	host: MachineHost<Evaluations.Effect>
) {
	from<Evaluations.State.Content> {
		on<Evaluations.Action.SelectWeek> { state, action ->
			state.weekItems
				.firstOrNull { item -> item.key == action.weekKey }
				?.let { selectedWeekItem ->
					state.copy(
						selectedWeekKey = selectedWeekItem.key,
						weekItem = selectedWeekItem
					)
				} ?: state
		}

		// Content stays on screen through observation errors and refresh cycles; the
		// machine-level rows move every other state instead.
		on<EvaluationsInternalEvent.EvaluationsObservationFailed> { state, _ -> state }

		on<EvaluationsInternalEvent.EvaluationsRefreshStarted> { state, _ -> state }

		on<EvaluationsInternalEvent.EvaluationsRefreshFailed> { state, _ -> state }
	}
}
