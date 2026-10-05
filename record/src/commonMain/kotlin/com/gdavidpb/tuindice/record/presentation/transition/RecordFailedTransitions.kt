package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent

internal fun MachineDefinitionBuilder<Record.State>.recordFailedTransitions() {
	from<Record.State.Failed> {
		// Keep-current-while-waiting: an unsynced empty observation keeps Failed visible, and
		// refreshes why it failed (a sync can learn the account is a new student later).
		on<RecordInternalEvent.RecordWaitingObserved> { state, event ->
			state.copy(isNewStudentNoRecord = event.isNewStudentNoRecord)
		}

		// The reason can arrive after the failure, or change with a later sync.
		on<RecordInternalEvent.NewStudentNoRecordObserved> { state, event ->
			state.copy(isNewStudentNoRecord = event.isNewStudentNoRecord)
		}
	}
}
