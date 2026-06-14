package com.gdavidpb.tuindice.subjects.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailInternalEvent
import com.gdavidpb.tuindice.subjects.presentation.mapper.withSelectedTab

internal fun MachineDefinitionBuilder<SubjectDetail.State>.contentTransitions() {
	from<SubjectDetail.State.Content> {
		on<SubjectDetail.Action.SelectSubjectSegmentTab> { state, action ->
			state.copy(detail = state.detail.withSelectedTab(action.tab))
		}

		on<SubjectDetailInternalEvent.DetailRefreshStarted> { state, _ -> state }

		on<SubjectDetailInternalEvent.DetailRefreshFailed> { state, _ -> state }
	}
}
