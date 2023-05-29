package com.gdavidpb.tuindice.base.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.contract.Browser

class BrowserViewModel :
	BaseViewModel<Browser.State, Browser.Action, Browser.Event>(initialViewState = Browser.State.Idle) {

	fun clickExternalResourceAction(url: String) =
		emitAction(Browser.Action.ClickExternalResource(url))

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
			is Browser.Action.ClickExternalResource ->
				sendEvent(Browser.Event.ShowExternalResourceDialog(url = action.url))
		}
	}
}