package com.gdavidpb.tuindice.login.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.login.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.login.presentation.contract.Splash
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class StartUpReducer :
	BaseReducer<Splash.State, Splash.Event, StartUpAction, StartUpError>() {

	override fun reduceUnrecoverableState(
		currentState: Splash.State,
		throwable: Throwable,
	): Flow<ViewOutput> {
		return flowOf(Splash.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Splash.State,
		useCaseState: UseCaseState.Loading<StartUpAction, StartUpError>
	): Flow<ViewOutput> {
		return flowOf(Splash.State.Starting)
	}

	override suspend fun reduceDataState(
		currentState: Splash.State,
		useCaseState: UseCaseState.Data<StartUpAction, StartUpError>
	): Flow<ViewOutput> {
		return flow {
			when (val data = useCaseState.value) {
				is StartUpAction.Main ->
					emit(Splash.Event.NavigateTo(navId = data.screen))

				is StartUpAction.SignIn ->
					emit(Splash.Event.NavigateToSignIn)
			}

			emit(Splash.State.Started)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Splash.State,
		useCaseState: UseCaseState.Error<StartUpAction, StartUpError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is StartUpError.NoServices ->
					emit(Splash.Event.ShowNoServicesDialog(status = error.status))

				else -> {}
			}

			emit(Splash.State.Failed)
		}
	}
}