package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermMachine
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem

class CreateSyntheticTermViewModel(
	override val screenMachine: CreateSyntheticTermMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<CreateSyntheticTerm.State, CreateSyntheticTerm.Action, CreateSyntheticTerm.Effect>(
	name = "create_synthetic_term",
	initialState = screenMachine.initialState(),
	initialAction = CreateSyntheticTerm.Action.Observe,
	dispatchers = dispatchers
) {
	fun configureAction(termId: String?) =
		sendAction(CreateSyntheticTerm.Action.ConfigureTerm(termId))

	fun updateQueryAction(
		query: String,
		selectionStart: Int = query.length,
		selectionEnd: Int = query.length
	) = sendAction(
		CreateSyntheticTerm.Action.UpdateQuery(
			query = query,
			selectionStart = selectionStart,
			selectionEnd = selectionEnd
		)
	)

	fun clearQueryAction() =
		updateQueryAction(
			query = "",
			selectionStart = 0,
			selectionEnd = 0
		)

	fun selectAddSubjectTabAction(tab: CreateTermAddSubjectTab) =
		sendAction(CreateSyntheticTerm.Action.SelectAddSubjectTab(tab))

	fun selectPeriodAction(termKey: String) =
		sendAction(CreateSyntheticTerm.Action.SelectPeriod(termKey))

	fun addSubjectAction(subjectItem: CreateTermSubjectItem) =
		sendAction(CreateSyntheticTerm.Action.AddSubject(subjectItem))

	fun removeSubjectAction(subjectCode: String) =
		sendAction(CreateSyntheticTerm.Action.RemoveSubject(subjectCode))

	fun createTermAction() {
		val currentState = state.value
		val period = currentState.selectedPeriod ?: return
		val subjects = currentState.selectedSubjects.map { item -> item.subject }
		if (subjects.isEmpty()) return

		sendAction(
			CreateSyntheticTerm.Action.CreateTerm(
				editingTermId = currentState.editingTermId,
				editingTermKey = currentState.editingTermKey,
				period = period,
				subjects = subjects
			)
		)
	}
}
