package com.gdavidpb.tuindice.login.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.base.utils.extension.config
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SignInReducer : BaseReducer<SignIn.State, SignIn.Event, Unit, SignInError>() {

	private val loadingMessages by config { getLoadingMessages() }

	override suspend fun reduceLoadingState(
		currentState: SignIn.State,
		useCaseState: UseCaseState.Loading<Unit, SignInError>
	): Flow<ViewOutput> {
		return flow {
			emit(SignIn.Event.ShakeLogo)
			emit(SignIn.Event.HideSoftKeyboard)

			emit(SignIn.State.LoggingIn(messages = loadingMessages.shuffled()))
		}
	}

	override suspend fun reduceDataState(
		currentState: SignIn.State,
		useCaseState: UseCaseState.Data<Unit, SignInError>
	): Flow<ViewOutput> {
		return flow {
			emit(SignIn.Event.NavigateToSplash)

			emit(SignIn.State.LoggedIn)
		}
	}

	override suspend fun reduceErrorState(
		currentState: SignIn.State,
		useCaseState: UseCaseState.Error<Unit, SignInError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is SignInError.EmptyPassword ->
					emit(SignIn.Event.ShowPasswordEmptyError)

				is SignInError.EmptyUsbId ->
					emit(SignIn.Event.ShowUsbIdEmptyError)

				is SignInError.InvalidUsbId ->
					emit(SignIn.Event.ShowUsbIdInvalidError)

				is SignInError.InvalidCredentials ->
					emit(SignIn.Event.ShowInvalidCredentialsSnackBar)

				is SignInError.AccountDisabled ->
					emit(SignIn.Event.ShowAccountDisabledSnackBar)

				is SignInError.NoConnection ->
					emit(SignIn.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

				is SignInError.Timeout ->
					emit(SignIn.Event.ShowTimeoutSnackBar)

				is SignInError.Unavailable ->
					emit(SignIn.Event.ShowUnavailableSnackBar)

				else ->
					emit(SignIn.Event.ShowDefaultErrorSnackBar)
			}

			emit(SignIn.State.Idle)
		}
	}
}