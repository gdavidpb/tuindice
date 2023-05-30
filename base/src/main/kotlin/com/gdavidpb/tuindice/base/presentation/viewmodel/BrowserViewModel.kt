package com.gdavidpb.tuindice.base.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.contract.Browser

class BrowserViewModel :
	BaseViewModel<Browser.State, Browser.Action, Browser.Event>(initialViewState = Browser.State.Idle) {

	fun openExternalResourceAction(url: String) =
		emitAction(Browser.Action.OpenExternalResource(url))

	fun confirmOpenExternalResourceAction(url: String) =
		emitAction(Browser.Action.ConfirmOpenExternalResource(url))

	fun closeDialogAction() =
		emitAction(Browser.Action.CloseDialog)

	fun showLoading() =
		setLoading(true)

	fun hideLoading() =
		setLoading(false)

	private fun setLoading(value: Boolean) {
		val currentState = getCurrentState()

		if (currentState is Browser.State.Content) {
			val newState = currentState.copy(isLoading = value)

			setState(newState)
		}
	}

	override suspend fun reducer(action: Browser.Action) {
		when (action) {
			is Browser.Action.OpenExternalResource ->
				sendEvent(Browser.Event.ShowExternalResourceDialog(url = action.url))

			is Browser.Action.ConfirmOpenExternalResource ->
				sendEvent(Browser.Event.OpenExternalResourceDialog(url = action.url))

			is Browser.Action.CloseDialog ->
				sendEvent(Browser.Event.CloseDialog)
		}
	}
}