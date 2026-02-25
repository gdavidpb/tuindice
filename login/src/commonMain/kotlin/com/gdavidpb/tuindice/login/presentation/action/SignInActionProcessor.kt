package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.repository.ConfigGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.mapper.toSignInParams
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SignInActionProcessor(
	private val signInUseCase: SignInUseCase,
	private val configRepository: ConfigGateway,
	private val textProvider: LoginTextProvider
) : ActionProcessor<SignIn.State, SignIn.Action.ClickSignIn, SignIn.Effect>() {

	private val loadingMessages by lazy {
		configRepository.getLoadingMessages()
	}

	override fun process(
		action: SignIn.Action.ClickSignIn,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		val params = action.toSignInParams()

		return signInUseCase.execute(params)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						if (state is SignIn.State.Idle)
							SignIn.State.LoggingIn(
								usbId = state.usbId,
								password = state.password,
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

					is UseCaseState.Error -> { state ->
						if (state is SignIn.State.LoggingIn) {
							when (val error = useCaseState.error) {
								is SignInUseCaseError.InvalidCredentials ->
									sideEffect(
										SignIn.Effect.ShowSnackBar(
											message = textProvider.invalidCredentials()
										)
									)

								is SignInUseCaseError.UserDisabled ->
									sideEffect(
										SignIn.Effect.ShowSnackBar(
											message = textProvider.userDisabled()
										)
									)

								is SignInUseCaseError.NoConnection ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = if (error.isNetworkAvailable)
												textProvider.serviceUnavailable()
											else
												textProvider.networkUnavailable(),
											actionLabel = textProvider.retry(),
											params = params
										)
									)

								is SignInUseCaseError.Timeout ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = textProvider.timeout(),
											actionLabel = textProvider.retry(),
											params = params
										)
									)

								is SignInUseCaseError.Unavailable ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = textProvider.serviceUnavailable(),
											actionLabel = textProvider.retry(),
											params = params
										)
									)

								else ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = textProvider.defaultError(),
											actionLabel = textProvider.retry(),
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
