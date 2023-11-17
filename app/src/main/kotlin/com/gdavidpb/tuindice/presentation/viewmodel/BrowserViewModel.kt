package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.presentation.action.browser.CloseBrowserDialogActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.ConfirmOpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.Flow

class BrowserViewModel(
	private val navigateToActionProcessor: NavigateToActionProcessor,
	private val setLoadingActionProcessor: SetLoadingActionProcessor,
	private val openExternalResourceActionProcessor: OpenExternalResourceActionProcessor,
	private val confirmOpenExternalResourceActionProcessor: ConfirmOpenExternalResourceActionProcessor,
	private val closeBrowserDialogActionProcessor: CloseBrowserDialogActionProcessor
) : BaseViewModel<Browser.State, Browser.Action, Browser.Effect>(initialState = Browser.State.Idle) {

	fun navigateToAction(url: String) =
		sendAction(Browser.Action.NavigateTo(url))

	fun openExternalResourceAction(url: String) =
		sendAction(Browser.Action.OpenExternalResource(url))

	fun confirmOpenExternalResourceAction(url: String) =
		sendAction(Browser.Action.ConfirmOpenExternalResource(url))

	fun closeDialogAction() =
		sendAction(Browser.Action.CloseDialog)

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

			is Browser.Action.ConfirmOpenExternalResource ->
				confirmOpenExternalResourceActionProcessor.process(action, sideEffect)

			is Browser.Action.CloseDialog ->
				closeBrowserDialogActionProcessor.process(action, sideEffect)
		}
	}
}