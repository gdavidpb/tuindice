package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
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
	private val openExternalResourceActionProcessor: OpenExternalResourceActionProcessor,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : BaseViewModel<Browser.State, Browser.Action, Browser.Effect>(
	name = "browser",
	initialState = Browser.State.Idle,
	dispatchers = dispatchers
) {
	fun navigateToAction(title: String, url: String) =
		sendAction(Browser.Action.NavigateTo(title, url))

	fun openExternalResourceAction(url: String) =
		sendAction(Browser.Action.OpenExternalResource(url))

	fun showLoadingAction() =
		sendAction(Browser.Action.SetLoading(true))

	fun hideLoadingAction() =
		sendAction(Browser.Action.SetLoading(false))

	override suspend fun processAction(
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
