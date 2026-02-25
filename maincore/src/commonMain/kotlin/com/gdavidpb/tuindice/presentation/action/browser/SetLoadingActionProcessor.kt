package com.gdavidpb.tuindice.presentation.action.browser

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetLoadingActionProcessor
	: ActionProcessor<Browser.State, Browser.Action.SetLoading, Browser.Effect>() {

	override fun process(
		action: Browser.Action.SetLoading,
		sideEffect: (Browser.Effect) -> Unit
	): Flow<Mutation<Browser.State>> {
		return flowOf { state ->
			if (state is Browser.State.Content)
				state.copy(
					isLoading = action.isLoading
				)
			else
				state
		}
	}
}