package com.gdavidpb.tuindice.base.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.contract.Browser

class BrowserViewModel :
	BaseViewModel<Browser.State, Browser.Action, Browser.Event>(initialViewState = Browser.State.Idle) {

	fun clickExternalResourceAction(url: String) =
		emitAction(Browser.Action.ClickExternalResource(url))

	fun closeExternalResourceDialogAction() =
		emitAction(Browser.Action.CloseExternalResourceDialog)

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
			is Browser.Action.ClickExternalResource -> {
				val currentState = getCurrentState()

				if (currentState is Browser.State.Content) {
					val newState = currentState.copy(
						externalResourceUrl = action.url,
						isShowExternalResourceDialog = true
					)

					setState(newState)
				}
			}

			is Browser.Action.CloseExternalResourceDialog -> {
				val currentState = getCurrentState()

				if (currentState is Browser.State.Content) {
					val newState = currentState.copy(
						externalResourceUrl = "",
						isShowExternalResourceDialog = false
					)

					setState(newState)
				}
			}
		}
	}
}