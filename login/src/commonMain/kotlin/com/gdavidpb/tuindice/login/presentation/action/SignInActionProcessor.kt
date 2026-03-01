package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.mapper.toSignInParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.error_invalid_credentials
import tuindice.login.generated.resources.error_user_disabled
import tuindice.login.generated.resources.label_retry
import tuindice.login.generated.resources.snack_default_error
import tuindice.login.generated.resources.snack_network_unavailable
import tuindice.login.generated.resources.snack_service_unavailable
import tuindice.login.generated.resources.snack_timeout

class SignInActionProcessor(
	private val signInUseCase: SignInUseCase,
	private val configRepository: ConfigRepository
) : ActionProcessor<SignIn.State, SignIn.Action.ClickSignIn, SignIn.Effect>() {

	private val loadingMessages by lazy {
		configRepository.getLoadingMessages()
	}

	override suspend fun process(
		action: SignIn.Action.ClickSignIn,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		val params = action.toSignInParams()
		val retryLabel = getString(Res.string.label_retry)

		return signInUseCase.execute(params)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						if (state is SignIn.State.Idle)
							SignIn.State.LoggingIn(
								usbId = params.usbId,
								password = params.password,
								messages = loadingMessages
							)
						else
							state
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							SignIn.Effect.NavigateToSummary
						)

						state
					}

					is UseCaseState.Error -> run {
						val error = useCaseState.error
						val errorMessage = when (error) {
							is SignInUseCaseError.InvalidCredentials ->
								getString(Res.string.error_invalid_credentials)

							is SignInUseCaseError.UserDisabled ->
								getString(Res.string.error_user_disabled)

							is SignInUseCaseError.NoConnection ->
								if (error.isNetworkAvailable)
									getString(Res.string.snack_service_unavailable)
								else
									getString(Res.string.snack_network_unavailable)

							is SignInUseCaseError.Timeout ->
								getString(Res.string.snack_timeout)

							is SignInUseCaseError.Unavailable ->
								getString(Res.string.snack_service_unavailable)

							else ->
								getString(Res.string.snack_default_error)
						}

						suspend { state: SignIn.State ->
							if (state is SignIn.State.LoggingIn) {
								when (error) {
									is SignInUseCaseError.InvalidCredentials,
									is SignInUseCaseError.UserDisabled ->
										sideEffect(
											SignIn.Effect.ShowSnackBar(
												message = errorMessage
											)
										)

									else ->
										sideEffect(
											SignIn.Effect.ShowRetrySnackBar(
												message = errorMessage,
												actionLabel = retryLabel,
												params = params
											)
										)
								}

								SignIn.State.Idle(
									usbId = state.usbId,
									password = state.password
								)
							} else
								state
						}
					}
				}
			}
	}
}
