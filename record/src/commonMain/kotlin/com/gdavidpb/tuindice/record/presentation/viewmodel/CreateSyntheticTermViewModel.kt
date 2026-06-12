package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermEditSeedUseCase
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermMachine
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CreateSyntheticTermViewModel(
	override val screenMachine: CreateSyntheticTermMachine,
	private val loadSyntheticTermEditSeedUseCase: LoadSyntheticTermEditSeedUseCase,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<CreateSyntheticTerm.State, CreateSyntheticTerm.Action, CreateSyntheticTerm.Effect>(
	name = "create_synthetic_term",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	private val queryFlow = MutableStateFlow("")
	private val selectedAddSubjectTabFlow = MutableStateFlow(CreateTermAddSubjectTab.Suggested)
	private val selectedSubjectsFlow = MutableStateFlow<List<SyntheticTermSubject>>(emptyList())
	private val selectedPeriodKeyFlow = MutableStateFlow<String?>(null)
	private val editingTermIdFlow = MutableStateFlow<String?>(null)
	private val editingTermKeyFlow = MutableStateFlow<String?>(null)
	private var configuredTermId: String? = null

	init {
		sendAction(
			CreateSyntheticTerm.Action.Observe(
				queryFlow = queryFlow,
				selectedAddSubjectTabFlow = selectedAddSubjectTabFlow,
				selectedSubjectsFlow = selectedSubjectsFlow,
				selectedPeriodKeyFlow = selectedPeriodKeyFlow,
				editingTermIdFlow = editingTermIdFlow,
				editingTermKeyFlow = editingTermKeyFlow
			)
		)
	}

	fun configureAction(termId: String?) {
		if (configuredTermId == termId) return
		configuredTermId = termId
		if (termId == null) {
			editingTermIdFlow.value = null
			editingTermKeyFlow.value = null
			selectedPeriodKeyFlow.value = null
			selectedSubjectsFlow.value = emptyList()
			return
		}

		viewModelScope.launch {
			loadSyntheticTermEditSeedUseCase.execute(termId).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					val seed = useCaseState.value
					selectedPeriodKeyFlow.value = seed.period.termKey
					selectedSubjectsFlow.value = seed.subjects
					editingTermKeyFlow.value = seed.termKey
					editingTermIdFlow.value = seed.termId
				}
			}
		}
	}

	fun updateQueryAction(
		query: String,
		selectionStart: Int = query.length,
		selectionEnd: Int = query.length
	) {
		queryFlow.value = query
		sendAction(
			CreateSyntheticTerm.Action.UpdateQuery(
				query = query,
				selectionStart = selectionStart,
				selectionEnd = selectionEnd
			)
		)
	}

	fun clearQueryAction() {
		updateQueryAction(
			query = "",
			selectionStart = 0,
			selectionEnd = 0
		)
	}

	fun selectAddSubjectTabAction(tab: CreateTermAddSubjectTab) {
		selectedAddSubjectTabFlow.value = tab
	}

	fun selectPeriodAction(termKey: String) {
		selectedPeriodKeyFlow.value = termKey
	}

	fun addSubjectAction(subjectItem: CreateTermSubjectItem) {
		val subject = subjectItem.subject
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
