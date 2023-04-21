package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.login.presentation.contract.Splash
import com.gdavidpb.tuindice.login.presentation.processor.StartUpReducer

class SplashViewModel(
	private val startUpReducer: StartUpReducer
) : BaseViewModel<Splash.State, Splash.Action, Splash.Event>(
	initialViewState = Splash.State.Starting
) {

	fun startUpAction(data: String) =
		emitAction(Splash.Action.StartUp(data = data))

	override suspend fun reducer(action: Splash.Action) {
		when (action) {
			is Splash.Action.StartUp ->
				startUpReducer.reduce(
					action = action,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)
		}
	}
}