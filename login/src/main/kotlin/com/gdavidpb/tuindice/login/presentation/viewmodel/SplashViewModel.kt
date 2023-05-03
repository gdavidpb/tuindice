package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.presentation.contract.Splash
import com.gdavidpb.tuindice.login.presentation.reducer.StartUpReducer

class SplashViewModel(
	private val startUpUseCase: StartUpUseCase,
	private val startUpReducer: StartUpReducer
) : BaseViewModel<Splash.State, Splash.Action, Splash.Event>(
	initialViewState = Splash.State.Starting
) {

	fun startUpAction(data: String) =
		emitAction(Splash.Action.StartUp(data = data))

	override suspend fun reducer(action: Splash.Action) {
		when (action) {
			is Splash.Action.StartUp ->
				startUpUseCase
					.execute(params = action.data)
					.collect(viewModel = this, reducer = startUpReducer)
		}
	}
}