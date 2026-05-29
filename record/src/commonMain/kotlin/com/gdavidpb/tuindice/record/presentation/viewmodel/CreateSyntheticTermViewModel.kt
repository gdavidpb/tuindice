package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermEditSeedUseCase
import com.gdavidpb.tuindice.record.presentation.action.CreateSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.ObserveCreateSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpdateCreateSyntheticTermQueryActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CreateSyntheticTermViewModel(
	private val observeCreateSyntheticTermActionProcessor: ObserveCreateSyntheticTermActionProcessor,
	private val updateCreateSyntheticTermQueryActionProcessor: UpdateCreateSyntheticTermQueryActionProcessor,
	private val createSyntheticTermActionProcessor: CreateSyntheticTermActionProcessor,
	private val loadSyntheticTermEditSeedUseCase: LoadSyntheticTermEditSeedUseCase,
	override val eventPublisher: EventPublisher
) : BaseViewModel<CreateSyntheticTerm.State, CreateSyntheticTerm.Action, CreateSyntheticTerm.Effect>(
	name = "create_synthetic_term",
	initialState = CreateSyntheticTerm.State()
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
				editingTermId = currentState.editingTermId,
				editingTermKey = currentState.editingTermKey,
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
