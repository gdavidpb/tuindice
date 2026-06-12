package com.gdavidpb.tuindice.subjects.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailInternalEvent

internal fun MachineDefinitionBuilder<SubjectDetail.State>.unavailableTransitions() {
	from<SubjectDetail.State.Unavailable> {
		on<SubjectDetailInternalEvent.DetailRefreshStarted> { state, _ -> state }

		on<SubjectDetailInternalEvent.DetailRefreshFailed> { state, _ -> state }
	}
}
