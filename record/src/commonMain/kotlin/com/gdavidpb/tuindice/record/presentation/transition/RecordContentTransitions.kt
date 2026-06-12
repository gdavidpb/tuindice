package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent

internal fun MachineDefinitionBuilder<Record.State>.recordContentTransitions(
	host: MachineHost<Record.Effect>
) {
	from<Record.State.Content> {
		on<RecordInternalEvent.RecordObservationFailed> { state, _ -> state }

		on<RecordInternalEvent.RecordRefreshStarted> { state, _ -> state }

		on<RecordInternalEvent.RecordRefreshFailed>(
			emits = setOf(Record.Effect.NavigateToOutdatedCredentials::class)
		) { state, event ->
			if (event.navigateToOutdatedCredentials) {
				host.sendEffect(Record.Effect.NavigateToOutdatedCredentials)
			}

			state
		}
	}
}
