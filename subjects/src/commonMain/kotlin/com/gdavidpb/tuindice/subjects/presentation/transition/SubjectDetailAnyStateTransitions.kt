package com.gdavidpb.tuindice.subjects.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailInternalEvent
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailMachine

internal fun MachineDefinitionBuilder<SubjectDetail.State>.anyStateTransitions(
	machine: SubjectDetailMachine,
	host: MachineHost<SubjectDetail.Effect>
) {
	fromAny {
		on<SubjectDetail.Action.LoadSubjectDetail> { state, action ->
			machine.startLoad(host = host, action = action)
			state
		}

		on<SubjectDetail.Action.RefreshSubjectDetail> { state, action ->
			machine.startRefresh(host = host, action = action)
			state
		}

		onTo<SubjectDetailInternalEvent.DetailLoadStarted, SubjectDetail.State.Loading> { _, _ ->
			SubjectDetail.State.Loading
		}

		onTo<SubjectDetailInternalEvent.DetailRefreshStarted, SubjectDetail.State.Loading> { _, _ ->
			SubjectDetail.State.Loading
		}

		onTo<SubjectDetailInternalEvent.DetailContentLoaded, SubjectDetail.State.Content> { _, event ->
			SubjectDetail.State.Content(detail = event.detail)
		}

		onTo<SubjectDetailInternalEvent.DetailUnavailableLoaded, SubjectDetail.State.Unavailable> { _, event ->
			SubjectDetail.State.Unavailable(subjectCode = event.subjectCode)
		}

		onTo<SubjectDetailInternalEvent.DetailLoadFailed, SubjectDetail.State.Failed> { _, event ->
			SubjectDetail.State.Failed(subjectCode = event.subjectCode)
		}

		onTo<SubjectDetailInternalEvent.DetailRefreshFailed, SubjectDetail.State.Failed> { _, event ->
			SubjectDetail.State.Failed(subjectCode = event.subjectCode)
		}
	}
}
