package com.gdavidpb.tuindice.presentation.action.browser

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NavigateToActionProcessor
	: ActionProcessor<Browser.State, Browser.Action.NavigateTo, Browser.Effect>() {

	override fun process(
		action: Browser.Action.NavigateTo,
		sideEffect: (Browser.Effect) -> Unit
	): Flow<Mutation<Browser.State>> {
		return flowOf { _ ->
			Browser.State.Content(
				url = action.url,
				isLoading = true
			)
		}
	}
}