package com.gdavidpb.tuindice.login.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseReducer
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword

class UpdatePasswordReducer(
	override val useCase: UpdatePasswordUseCase
) : BaseReducer<String, Unit, SignInError, UpdatePassword.State, UpdatePassword.Action.ClickSignIn, UpdatePassword.Event>() {
	override fun actionToParams(action: UpdatePassword.Action.ClickSignIn): String {
		return action.password
	}

	override suspend fun reduceLoadingState(
		state: UseCaseState.Loading<Unit, SignInError>,
		eventProducer: (UpdatePassword.Event) -> Unit
	): UpdatePassword.State {
		eventProducer(UpdatePassword.Event.HideSoftKeyboard)

		return UpdatePassword.State.LoggingIn
	}

	override suspend fun reduceDataState(
		state: UseCaseState.Data<Unit, SignInError>,
		eventProducer: (UpdatePassword.Event) -> Unit
	): UpdatePassword.State {
		return UpdatePassword.State.LoggedIn
	}

	override suspend fun reduceErrorState(
		state: UseCaseState.Error<Unit, SignInError>,
		eventProducer: (UpdatePassword.Event) -> Unit
	): UpdatePassword.State {
		when (val error = state.error) {
			is SignInError.AccountDisabled ->
				eventProducer(UpdatePassword.Event.NavigateToAccountDisabled)

			is SignInError.EmptyPassword ->
				eventProducer(UpdatePassword.Event.ShowPasswordUpdatedToast)

			is SignInError.InvalidCredentials ->
				eventProducer(UpdatePassword.Event.ShowInvalidCredentialsError)

			is SignInError.NoConnection ->
				eventProducer(UpdatePassword.Event.ShowNoConnectionError(isNetworkAvailable = error.isNetworkAvailable))

			is SignInError.Timeout ->
				eventProducer(UpdatePassword.Event.ShowTimeoutError)

			is SignInError.Unavailable ->
				eventProducer(UpdatePassword.Event.ShowUnavailableError)

			else -> {}
		}

		return UpdatePassword.State.Idle
	}
}