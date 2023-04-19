package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInWith
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.presentation.contract.Splash
import com.gdavidpb.tuindice.login.presentation.processor.SplashStartUpProcessor

class SplashViewModel(
	private val startUpUseCase: StartUpUseCase
) : BaseViewModel<Splash.State, Splash.Action, Splash.Event>(initialViewState = Splash.State.Starting) {

	fun startUpAction(data: String) = emitAction(Splash.Action.StartUp(data))

	override suspend fun handleAction(action: Splash.Action) {
		when (action) {
			is Splash.Action.StartUp -> stateInWith(
				useCase = startUpUseCase,
				params = action.data,
				processor = SplashStartUpProcessor { event -> sendEvent(event) }
			)
		}
	}
}