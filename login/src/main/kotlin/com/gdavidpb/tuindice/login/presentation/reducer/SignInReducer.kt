package com.gdavidpb.tuindice.login.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.base.utils.extension.config
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.route.ACTION_ID_RETRY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class SignInReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<SignIn.State, SignIn.Event, Unit, SignInError>() {

	private val loadingMessages by config { getLoadingMessages() }

	override suspend fun reduceLoadingState(
		currentState: SignIn.State,
		useCaseState: UseCaseState.Loading<Unit, SignInError>
	): Flow<ViewOutput> {
		return if (currentState is SignIn.State.Idle)
			flowOf(
				SignIn.State.LoggingIn(
					usbId = currentState.usbId,
					password = currentState.password,
					messages = loadingMessages
				)
			)
		else
			super.reduceLoadingState(currentState, useCaseState)
	}

	override suspend fun reduceDataState(
		currentState: SignIn.State,
		useCaseState: UseCaseState.Data<Unit, SignInError>
	): Flow<ViewOutput> {
		return flowOf(SignIn.Event.NavigateToSummary)
	}

	override suspend fun reduceErrorState(
		currentState: SignIn.State,
		useCaseState: UseCaseState.Error<Unit, SignInError>
	): Flow<ViewOutput> {
		return if (currentState is SignIn.State.LoggingIn)
			flow {
				when (val error = useCaseState.error) {
					is SignInError.InvalidCredentials ->
						emit(
							SignIn.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_invalid_credentials)
							)
						)

					is SignInError.AccountDisabled ->
						emit(
							SignIn.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_account_disabled)
							)
						)

					is SignInError.NoConnection ->
						emit(
							SignIn.Event.ShowSnackBar(
								message = if (error.isNetworkAvailable)
									resourceResolver.getString(R.string.snack_service_unavailable)
								else
									resourceResolver.getString(R.string.snack_network_unavailable),
								actionLabel = resourceResolver.getString(R.string.retry),
								actionId = ACTION_ID_RETRY
							)
						)

					is SignInError.Timeout ->
						emit(
							SignIn.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_timeout),
								actionLabel = resourceResolver.getString(R.string.retry),
								actionId = ACTION_ID_RETRY
							)
						)

					is SignInError.Unavailable ->
						emit(
							SignIn.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_service_unavailable),
								actionLabel = resourceResolver.getString(R.string.retry),
								actionId = ACTION_ID_RETRY
							)
						)

					else ->
						emit(
							SignIn.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_default_error),
								actionLabel = resourceResolver.getString(R.string.retry),
								actionId = ACTION_ID_RETRY
							)
						)
				}

				emit(
					SignIn.State.Idle(
						usbId = currentState.usbId,
						password = currentState.password
					)
				)
			}
		else
			super.reduceErrorState(currentState, useCaseState)
	}
}