package com.gdavidpb.tuindice.pensum.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumInternalEvent

internal fun MachineDefinitionBuilder<Pensum.State>.emptyTransitions() {
	from<Pensum.State.Empty> {
		on<PensumInternalEvent.PensumDataMissing> { state, _ -> state }

		on<PensumInternalEvent.PensumRecordDataUnavailableObserved> { state, _ -> state }

		// The backend answered that this student has no pensum; asking on every entry would only
		// repeat that answer. The next screen instance (a new launch) asks again.
		on<Pensum.Action.EnsurePensumLoaded> { state, _ -> state }
	}
}
