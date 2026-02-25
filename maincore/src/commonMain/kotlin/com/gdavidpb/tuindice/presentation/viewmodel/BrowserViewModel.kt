package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.Flow

class BrowserViewModel(
	private val navigateToActionProcessor: NavigateToActionProcessor,
	private val setLoadingActionProcessor: SetLoadingActionProcessor,
	private val openExternalResourceActionProcessor: OpenExternalResourceActionProcessor
) : BaseViewModel<Browser.State, Browser.Action, Browser.Effect>(initialState = Browser.State.Idle) {

	fun navigateToAction(title: String, url: String) =
		sendAction(Browser.Action.NavigateTo(title, url))

	fun openExternalResourceAction(url: String) =
		sendAction(Browser.Action.OpenExternalResource(url))

	fun showLoadingAction() =
		sendAction(Browser.Action.SetLoading(true))

	fun hideLoadingAction() =
		sendAction(Browser.Action.SetLoading(false))

	override fun processAction(
		action: Browser.Action,
		sideEffect: (Browser.Effect) -> Unit
	): Flow<Mutation<Browser.State>> {
		return when (action) {
			is Browser.Action.NavigateTo ->
				navigateToActionProcessor.process(action, sideEffect)

			is Browser.Action.SetLoading ->
				setLoadingActionProcessor.process(action, sideEffect)

			is Browser.Action.OpenExternalResource ->
				openExternalResourceActionProcessor.process(action, sideEffect)
		}
	}
}