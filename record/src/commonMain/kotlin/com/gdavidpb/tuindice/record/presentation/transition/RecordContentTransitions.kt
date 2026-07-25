package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine

internal fun MachineDefinitionBuilder<Record.State>.recordContentTransitions(
	machine: RecordMachine,
	host: MachineHost<Record.Effect>
) {
	from<Record.State.Content> {
		// Selecting a term only makes sense with content on screen: the row reads the
		// view mode from S instead of trusting a payload copy, and any other state
		// rejects the action with telemetry.
		on<Record.Action.SelectTerm> { state, action ->
			machine.selectTerm(
				host = host,
				termId = action.termId,
				viewMode = state.viewMode
			)
			state
		}

		on<RecordInternalEvent.RecordObservationFailed> { state, _ -> state }

		on<RecordInternalEvent.RecordRefreshStarted> { state, _ -> state }

		// A failed refresh keeps the on-screen record; the user still gets told,
		// mirroring the Summary refresh-failure feedback.
		on<RecordInternalEvent.RecordRefreshFailed>(
			emits = setOf(
				Record.Effect.NavigateToOutdatedCredentials::class,
				Record.Effect.ShowSnackBar::class
			)
		) { state, event ->
			if (event.navigateToOutdatedCredentials) {
				host.sendEffect(Record.Effect.NavigateToOutdatedCredentials)
			} else {
				host.sendEffect(Record.Effect.ShowSnackBar(message = event.message))
			}

			state
		}
	}
}
