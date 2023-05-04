package com.gdavidpb.tuindice.login.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class UpdatePasswordReducer :
	BaseReducer<UpdatePassword.State, UpdatePassword.Event, Unit, SignInError>() {

	override suspend fun reduceLoadingState(
		currentState: UpdatePassword.State,
		useCaseState: UseCaseState.Loading<Unit, SignInError>
	): Flow<ViewOutput> {
		return flow {
			emit(UpdatePassword.Event.HideSoftKeyboard)

			emit(UpdatePassword.State.LoggingIn)
		}
	}

	override suspend fun reduceDataState(
		currentState: UpdatePassword.State,
		useCaseState: UseCaseState.Data<Unit, SignInError>
	): Flow<ViewOutput> {
		return flowOf(UpdatePassword.State.LoggedIn)
	}

	override suspend fun reduceErrorState(
		currentState: UpdatePassword.State,
		useCaseState: UseCaseState.Error<Unit, SignInError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is SignInError.EmptyPassword ->
					emit(UpdatePassword.Event.ShowPasswordUpdatedToast)

				is SignInError.InvalidCredentials ->
					emit(UpdatePassword.Event.ShowInvalidCredentialsError)

				is SignInError.NoConnection ->
					emit(UpdatePassword.Event.ShowNoConnectionError(isNetworkAvailable = error.isNetworkAvailable))

				is SignInError.Timeout ->
					emit(UpdatePassword.Event.ShowTimeoutError)

				is SignInError.Unavailable ->
					emit(UpdatePassword.Event.ShowUnavailableError)

				else -> {}
			}

			emit(UpdatePassword.State.Idle)
		}
	}
}