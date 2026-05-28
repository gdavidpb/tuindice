package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.subjects.presentation.action.ObserveSubjectSearchActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.RetrySubjectSearchActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.UpdateSubjectSearchQueryActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SubjectSearchViewModel(
	private val observeSubjectSearchActionProcessor: ObserveSubjectSearchActionProcessor,
	private val updateSubjectSearchQueryActionProcessor: UpdateSubjectSearchQueryActionProcessor,
	private val retrySubjectSearchActionProcessor: RetrySubjectSearchActionProcessor
) : BaseViewModel<SubjectSearch.State, SubjectSearch.Action, SubjectSearch.Effect>(
	initialState = SubjectSearch.State()
) {
	private val queryFlow = MutableStateFlow("")

	init {
		sendAction(SubjectSearch.Action.ObserveSubjectSearch(queryFlow = queryFlow))
	}

	fun updateQueryAction(query: String) {
		queryFlow.value = query
		sendAction(SubjectSearch.Action.UpdateQuery(query = query))
	}

	fun retryAction() {
		sendAction(SubjectSearch.Action.Retry(query = queryFlow.value))
	}

	override suspend fun processAction(
		action: SubjectSearch.Action,
		sideEffect: (SubjectSearch.Effect) -> Unit
	): Flow<Mutation<SubjectSearch.State>> {
		return when (action) {
			is SubjectSearch.Action.ObserveSubjectSearch ->
				observeSubjectSearchActionProcessor.process(action, sideEffect)

			is SubjectSearch.Action.UpdateQuery ->
				updateSubjectSearchQueryActionProcessor.process(action, sideEffect)

			is SubjectSearch.Action.Retry ->
				retrySubjectSearchActionProcessor.process(action, sideEffect)
		}
	}
}
