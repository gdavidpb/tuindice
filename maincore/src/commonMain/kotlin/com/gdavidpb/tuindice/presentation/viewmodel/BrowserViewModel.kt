package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.machine.BrowserMachine

class BrowserViewModel(
	override val screenMachine: BrowserMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Browser.State, Browser.Action, Browser.Effect>(
	name = "browser",
	initialState = screenMachine.initialState(),
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
}
