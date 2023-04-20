package com.gdavidpb.tuindice.login.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseProcessor
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword

class UpdatePasswordProcessor(
	override val eventChannel: (UpdatePassword.Event) -> Unit
) :
	BaseProcessor<Unit, SignInError, UpdatePassword.State, UpdatePassword.Event>(eventChannel) {
	override suspend fun processLoadingState(state: UseCaseState.Loading<Unit, SignInError>): UpdatePassword.State {
		eventChannel(UpdatePassword.Event.HideSoftKeyboard)

		return UpdatePassword.State.LoggingIn
	}

	override suspend fun processDataState(state: UseCaseState.Data<Unit, SignInError>): UpdatePassword.State {
		return UpdatePassword.State.LoggedIn
	}

	override suspend fun processErrorState(state: UseCaseState.Error<Unit, SignInError>): UpdatePassword.State {
		when (val error = state.error) {
			is SignInError.AccountDisabled ->
				eventChannel(UpdatePassword.Event.NavigateToAccountDisabled)

			is SignInError.EmptyPassword ->
				eventChannel(UpdatePassword.Event.ShowPasswordUpdatedToast)

			is SignInError.InvalidCredentials ->
				eventChannel(UpdatePassword.Event.ShowInvalidCredentialsError)

			is SignInError.NoConnection ->
				eventChannel(UpdatePassword.Event.ShowNoConnectionError(isNetworkAvailable = error.isNetworkAvailable))

			is SignInError.Timeout ->
				eventChannel(UpdatePassword.Event.ShowTimeoutError)

			is SignInError.Unavailable ->
				eventChannel(UpdatePassword.Event.ShowUnavailableError)

			else -> {}
		}

		return UpdatePassword.State.Idle
	}
}