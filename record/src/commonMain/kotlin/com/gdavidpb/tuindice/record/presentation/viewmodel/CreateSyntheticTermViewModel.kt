package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.presentation.action.CreateSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.ObserveCreateSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpdateCreateSyntheticTermQueryActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class CreateSyntheticTermViewModel(
	private val observeCreateSyntheticTermActionProcessor: ObserveCreateSyntheticTermActionProcessor,
	private val updateCreateSyntheticTermQueryActionProcessor: UpdateCreateSyntheticTermQueryActionProcessor,
	private val createSyntheticTermActionProcessor: CreateSyntheticTermActionProcessor
) : BaseViewModel<CreateSyntheticTerm.State, CreateSyntheticTerm.Action, CreateSyntheticTerm.Effect>(
	initialState = CreateSyntheticTerm.State()
) {
	private val queryFlow = MutableStateFlow("")
	private val selectedSubjectsFlow = MutableStateFlow<List<SyntheticTermSubject>>(emptyList())
	private val selectedPeriodKeyFlow = MutableStateFlow<String?>(null)

	init {
		sendAction(
			CreateSyntheticTerm.Action.Observe(
				queryFlow = queryFlow,
				selectedSubjectsFlow = selectedSubjectsFlow,
				selectedPeriodKeyFlow = selectedPeriodKeyFlow
			)
		)
	}

	fun updateQueryAction(query: String) {
		queryFlow.value = query
		sendAction(CreateSyntheticTerm.Action.UpdateQuery(query = query))
	}

	fun clearQueryAction() {
		updateQueryAction("")
	}

	fun selectPeriodAction(termKey: String) {
		selectedPeriodKeyFlow.value = termKey
	}

	fun addSubjectAction(subject: SyntheticTermSubject) {
		if (!subject.canAdd) return
		val current = selectedSubjectsFlow.value
		if (current.any { item -> item.subjectCode == subject.subjectCode }) return
		selectedSubjectsFlow.value = current + subject
	}

	fun removeSubjectAction(subjectCode: String) {
		selectedSubjectsFlow.value = selectedSubjectsFlow.value
			.filterNot { subject -> subject.subjectCode == subjectCode }
	}

	fun createTermAction() {
		val currentState = state.value
		val period = currentState.selectedPeriod ?: return
		val subjects = currentState.selectedSubjects
		if (subjects.isEmpty()) return

		sendAction(
			CreateSyntheticTerm.Action.CreateTerm(
				period = period,
				subjects = subjects
			)
		)
	}

	override suspend fun processAction(
		action: CreateSyntheticTerm.Action,
		sideEffect: (CreateSyntheticTerm.Effect) -> Unit
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return when (action) {
			is CreateSyntheticTerm.Action.Observe ->
				observeCreateSyntheticTermActionProcessor.process(action, sideEffect)

			is CreateSyntheticTerm.Action.UpdateQuery ->
				updateCreateSyntheticTermQueryActionProcessor.process(action, sideEffect)

			is CreateSyntheticTerm.Action.CreateTerm ->
				createSyntheticTermActionProcessor.process(action, sideEffect)
		}
	}
}
