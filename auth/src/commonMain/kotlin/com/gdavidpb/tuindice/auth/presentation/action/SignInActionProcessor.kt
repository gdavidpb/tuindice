package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.mapper.toSignInParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_credentials
import tuindice.auth.generated.resources.error_untrusted
import tuindice.auth.generated.resources.label_retry
import tuindice.auth.generated.resources.snack_default_error
import tuindice.auth.generated.resources.snack_network_unavailable
import tuindice.auth.generated.resources.snack_sign_in_failed
import tuindice.auth.generated.resources.snack_service_unavailable
import tuindice.auth.generated.resources.snack_timeout

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
					is UseCaseState.Loading -> suspend { state ->
						if (state is SignIn.State.Idle)
							SignIn.State.LoggingIn(
								usbId = params.usbId,
								password = params.password,
								messages = loadingMessages,
								analyticsCollectionEnabled = state.analyticsCollectionEnabled
							)
						else
							state
					}

					is UseCaseState.Data -> suspend { state ->
						sideEffect(
							SignIn.Effect.NavigateToSummary
						)

						state
					}

					is UseCaseState.Error -> suspend { state: SignIn.State ->
						val error = useCaseState.error
						val errorMessage = when (error) {
							is SignInUseCaseError.InvalidCredentials ->
								getString(Res.string.error_invalid_credentials)

							is SignInUseCaseError.AccountDisabled ->
								getString(Res.string.error_account_disabled)

							is SignInUseCaseError.Untrusted ->
								getString(Res.string.error_untrusted)

							is SignInUseCaseError.AuthenticationFailed ->
								getString(Res.string.snack_sign_in_failed)

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

						if (state is SignIn.State.LoggingIn) {
							when (error) {
								is SignInUseCaseError.InvalidCredentials,
								is SignInUseCaseError.AccountDisabled,
								is SignInUseCaseError.Untrusted ->
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
								password = state.password,
								analyticsCollectionEnabled = state.analyticsCollectionEnabled
							)
						} else
							state
					}
				}
			}
	}
}
