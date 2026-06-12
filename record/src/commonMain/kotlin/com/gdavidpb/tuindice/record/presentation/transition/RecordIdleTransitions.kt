package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine

internal fun MachineDefinitionBuilder<Record.State>.recordIdleTransitions(
	machine: RecordMachine,
	host: MachineHost<Record.Effect>
) {
	from<Record.State.Idle> {
		on<Record.Action.ObserveRecord> { state, _ ->
			machine.startObservation(host = host)
			state
		}
	}
}
