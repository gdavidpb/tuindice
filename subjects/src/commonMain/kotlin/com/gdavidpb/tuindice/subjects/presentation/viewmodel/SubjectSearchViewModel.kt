package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchMachine

class SubjectSearchViewModel(
	override val screenMachine: SubjectSearchMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SubjectSearch.State, SubjectSearch.Action, SubjectSearch.Effect>(
	name = "subject_search",
	initialState = screenMachine.initialState(),
	initialAction = SubjectSearch.Action.ObserveSubjectSearch,
	dispatchers = dispatchers
) {
	fun updateQueryAction(query: String) =
		sendAction(SubjectSearch.Action.UpdateQuery(query = query))

	fun retryAction() =
		sendAction(SubjectSearch.Action.Retry)
}
