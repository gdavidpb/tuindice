package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateSubjectSearchQueryActionProcessor : ActionProcessor<
	SubjectSearch.State,
	SubjectSearch.Action.UpdateQuery,
	SubjectSearch.Effect
	>() {
	override suspend fun process(
		action: SubjectSearch.Action.UpdateQuery,
		sideEffect: (SubjectSearch.Effect) -> Unit
	): Flow<Mutation<SubjectSearch.State>> {
		return flowOf(
			suspend { state: SubjectSearch.State ->
				if (action.query.trim().length < MinimumSubjectSearchQueryLength) {
					state.copy(
						query = action.query,
						results = emptyList(),
						isRefreshing = false,
						hasRemoteError = false
					)
				} else {
					state.copy(
						query = action.query,
						hasRemoteError = false
					)
				}
			}
		)
	}
}

internal const val MinimumSubjectSearchQueryLength = 2
internal const val SubjectSearchLimit = 20
internal const val SubjectSearchDebounceMillis = 300L
