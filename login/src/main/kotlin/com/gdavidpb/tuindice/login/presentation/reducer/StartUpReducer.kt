package com.gdavidpb.tuindice.login.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.login.presentation.contract.Splash

class StartUpReducer(
	override val useCase: StartUpUseCase
) : BaseReducer<String, StartUpAction, StartUpError, Splash.State, Splash.Action.StartUp, Splash.Event>() {

	override fun actionToParams(action: Splash.Action.StartUp): String {
		return action.data
	}

	override suspend fun reduceLoadingState(
		state: UseCaseState.Loading<StartUpAction, StartUpError>,
		eventProducer: (Splash.Event) -> Unit
	): Splash.State {
		return Splash.State.Starting
	}

	override suspend fun reduceDataState(
		state: UseCaseState.Data<StartUpAction, StartUpError>,
		eventProducer: (Splash.Event) -> Unit
	): Splash.State {
		when (val data = state.value) {
			is StartUpAction.Main ->
				eventProducer(Splash.Event.NavigateTo(navId = data.screen))

			is StartUpAction.SignIn ->
				eventProducer(Splash.Event.NavigateToSignIn)
		}

		return Splash.State.Started
	}

	override suspend fun reduceErrorState(
		state: UseCaseState.Error<StartUpAction, StartUpError>,
		eventProducer: (Splash.Event) -> Unit
	): Splash.State {
		when (val error = state.error) {
			is StartUpError.NoServices ->
				eventProducer(Splash.Event.ShowNoServicesDialog(status = error.status))

			else -> {}
		}

		return Splash.State.Failed
	}
}