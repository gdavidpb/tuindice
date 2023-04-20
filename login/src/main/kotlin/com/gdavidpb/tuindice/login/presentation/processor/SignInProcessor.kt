package com.gdavidpb.tuindice.login.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseProcessor
import com.gdavidpb.tuindice.base.utils.extension.config
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.SignIn

class SignInProcessor(
	override val eventChannel: (SignIn.Event) -> Unit
) : BaseProcessor<Unit, SignInError, SignIn.State, SignIn.Event>(eventChannel) {

	private val loadingMessages by config { getLoadingMessages() }

	override suspend fun processDataState(state: UseCaseState.Data<Unit, SignInError>): SignIn.State {
		return SignIn.State.LoggedIn
	}

	override suspend fun processLoadingState(state: UseCaseState.Loading<Unit, SignInError>): SignIn.State {
		eventChannel(SignIn.Event.ShakeLogo)
		eventChannel(SignIn.Event.HideSoftKeyboard)

		return SignIn.State.SigningIn(messages = loadingMessages)
	}

	override suspend fun processErrorState(state: UseCaseState.Error<Unit, SignInError>): SignIn.State {
		return when (val error = state.error) {
			is SignInError.EmptyPassword -> {
				eventChannel(SignIn.Event.ShowPasswordFieldEmptyError)

				SignIn.State.Idle
			}

			is SignInError.EmptyUsbId -> {
				eventChannel(SignIn.Event.ShowUsbIdFieldEmptyError)

				SignIn.State.Idle
			}

			is SignInError.InvalidUsbId -> {
				eventChannel(SignIn.Event.ShowUsbIdFieldInvalidError)

				SignIn.State.Idle
			}

			is SignInError.InvalidCredentials -> {
				eventChannel(SignIn.Event.ShowInvalidCredentialsSnackBar)

				SignIn.State.Idle
			}

			is SignInError.AccountDisabled -> {
				eventChannel(SignIn.Event.ShowAccountDisabledSnackBar)

				SignIn.State.Idle
			}

			is SignInError.NoConnection -> {
				eventChannel(SignIn.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

				SignIn.State.Idle
			}

			is SignInError.Timeout -> {
				eventChannel(SignIn.Event.ShowTimeoutSnackBar)

				SignIn.State.Idle
			}

			is SignInError.Unavailable -> {
				eventChannel(SignIn.Event.ShowUnavailableSnackBar)

				SignIn.State.Idle
			}

			else -> SignIn.State.Idle
		}
	}
}