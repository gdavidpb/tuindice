package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent

internal fun MachineDefinitionBuilder<Record.State>.recordFailedTransitions() {
	from<Record.State.Failed> {
		// Keep-current-while-waiting: an unsynced empty observation keeps Failed visible.
		on<RecordInternalEvent.RecordWaitingObserved> { state, _ -> state }
	}
}
