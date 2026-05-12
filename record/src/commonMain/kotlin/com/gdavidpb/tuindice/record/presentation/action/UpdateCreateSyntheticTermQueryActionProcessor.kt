package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateCreateSyntheticTermQueryActionProcessor : ActionProcessor<
	CreateSyntheticTerm.State,
	CreateSyntheticTerm.Action.UpdateQuery,
	CreateSyntheticTerm.Effect
	>() {
	override suspend fun process(
		action: CreateSyntheticTerm.Action.UpdateQuery,
		sideEffect: (CreateSyntheticTerm.Effect) -> Unit
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return flowOf(
			suspend { state ->
				if (action.query.trim().length < 2) {
					state.copy(
						query = action.query,
						searchResults = emptyList(),
						isRefreshingSearch = false,
						hasSearchError = false
					)
				} else {
					state.copy(
						query = action.query,
						hasSearchError = false
					)
				}
			}
		)
	}
}
