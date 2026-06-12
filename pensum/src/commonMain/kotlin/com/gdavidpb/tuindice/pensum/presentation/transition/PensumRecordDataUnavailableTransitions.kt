package com.gdavidpb.tuindice.pensum.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumInternalEvent

internal fun MachineDefinitionBuilder<Pensum.State>.recordDataUnavailableTransitions() {
	from<Pensum.State.RecordDataUnavailable> {
		on<PensumInternalEvent.PensumDataMissing> { state, _ -> state }

		on<PensumInternalEvent.PensumRecordDataUnavailableObserved> { state, _ -> state }
	}
}
