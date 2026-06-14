package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailMachine

class SubjectDetailViewModel(
	override val screenMachine: SubjectDetailMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SubjectDetail.State, SubjectDetail.Action, SubjectDetail.Effect>(
	name = "subject_detail",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun loadSubjectDetailAction(subjectCode: String) {
		sendAction(SubjectDetail.Action.LoadSubjectDetail(subjectCode = subjectCode))
	}

	fun refreshSubjectDetailAction(subjectCode: String) {
		sendAction(SubjectDetail.Action.RefreshSubjectDetail(subjectCode = subjectCode))
	}

	fun selectSubjectSegmentTabAction(tab: SubjectSegmentTab) {
		sendAction(SubjectDetail.Action.SelectSubjectSegmentTab(tab = tab))
	}

}
