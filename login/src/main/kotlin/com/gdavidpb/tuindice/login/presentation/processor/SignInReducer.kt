package com.gdavidpb.tuindice.login.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.config
import com.gdavidpb.tuindice.login.domain.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.SignIn

class SignInReducer(
	override val useCase: SignInUseCase
) : BaseReducer<SignInParams, Unit, SignInError, SignIn.State, SignIn.Action.ClickSignIn, SignIn.Event>() {

	private val loadingMessages by config { getLoadingMessages() }

	override fun actionToParams(action: SignIn.Action.ClickSignIn): SignInParams {
		return SignInParams(usbId = action.usbId, password = action.password)
	}

	override suspend fun reduceLoadingState(
		state: UseCaseState.Loading<Unit, SignInError>,
		eventProducer: (SignIn.Event) -> Unit
	): SignIn.State {
		eventProducer(SignIn.Event.ShakeLogo)
		eventProducer(SignIn.Event.HideSoftKeyboard)

		return SignIn.State.LoggingIn(messages = loadingMessages.shuffled())
	}

	override suspend fun reduceDataState(
		state: UseCaseState.Data<Unit, SignInError>,
		eventProducer: (SignIn.Event) -> Unit
	): SignIn.State {
		eventProducer(SignIn.Event.NavigateToSplash)

		return SignIn.State.LoggedIn
	}

	override suspend fun reduceErrorState(
		state: UseCaseState.Error<Unit, SignInError>,
		eventProducer: (SignIn.Event) -> Unit
	): SignIn.State {
		when (val error = state.error) {
			is SignInError.EmptyPassword ->
				eventProducer(SignIn.Event.ShowPasswordEmptyError)

			is SignInError.EmptyUsbId ->
				eventProducer(SignIn.Event.ShowUsbIdEmptyError)

			is SignInError.InvalidUsbId ->
				eventProducer(SignIn.Event.ShowUsbIdInvalidError)

			is SignInError.InvalidCredentials ->
				eventProducer(SignIn.Event.ShowInvalidCredentialsSnackBar)

			is SignInError.AccountDisabled ->
				eventProducer(SignIn.Event.ShowAccountDisabledSnackBar)

			is SignInError.NoConnection ->
				eventProducer(SignIn.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

			is SignInError.Timeout ->
				eventProducer(SignIn.Event.ShowTimeoutSnackBar)

			is SignInError.Unavailable ->
				eventProducer(SignIn.Event.ShowUnavailableSnackBar)

			else ->
				eventProducer(SignIn.Event.ShowDefaultErrorSnackBar)
		}

		return SignIn.State.Idle
	}
}