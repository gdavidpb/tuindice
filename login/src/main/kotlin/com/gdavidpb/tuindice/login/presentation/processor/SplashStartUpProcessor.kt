package com.gdavidpb.tuindice.login.presentation.processor

import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseProcessor
import com.gdavidpb.tuindice.login.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.login.presentation.contract.Splash

class SplashStartUpProcessor(
	override val eventChannel: (Splash.Event) -> Unit
) : BaseProcessor<StartUpAction, StartUpError, Splash.State, Splash.Event>(eventChannel) {
	override suspend fun processLoadingState(state: UseCaseState.Loading<StartUpAction, StartUpError>): Splash.State {
		return Splash.State.Starting
	}

	override suspend fun processDataState(state: UseCaseState.Data<StartUpAction, StartUpError>): Splash.State {
		when (val data = state.value) {
			is StartUpAction.Main ->
				eventChannel(Splash.Event.NavigateTo(navId = data.screen))

			is StartUpAction.SignIn ->
				eventChannel(Splash.Event.NavigateToSignIn)
		}

		return Splash.State.Started
	}

	override suspend fun processErrorState(state: UseCaseState.Error<StartUpAction, StartUpError>): Splash.State {
		when (val error = state.error) {
			is StartUpError.NoServices ->
				eventChannel(Splash.Event.ShowNoServicesDialog(status = error.status))

			else -> {}
		}

		return Splash.State.Failed
	}
}