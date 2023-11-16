package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.config
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.mapper.toSignInParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SignInActionProcessor(
	private val signInUseCase: SignInUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<SignIn.State, SignIn.Action.ClickSignIn, SignIn.Effect>() {

	private val loadingMessages by config { getLoadingMessages() }

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
								is SignInError.InvalidCredentials ->
									sideEffect(
										SignIn.Effect.ShowSnackBar(
											message = resourceResolver.getString(R.string.snack_invalid_credentials)
										)
									)

								is SignInError.AccountDisabled ->
									sideEffect(
										SignIn.Effect.ShowSnackBar(
											message = resourceResolver.getString(R.string.snack_account_disabled)
										)
									)

								is SignInError.NoConnection ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = if (error.isNetworkAvailable)
												resourceResolver.getString(R.string.snack_service_unavailable)
											else
												resourceResolver.getString(R.string.snack_network_unavailable),
											actionLabel = resourceResolver.getString(R.string.retry),
											params = params
										)
									)

								is SignInError.Timeout ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = resourceResolver.getString(R.string.snack_timeout),
											actionLabel = resourceResolver.getString(R.string.retry),
											params = params
										)
									)

								is SignInError.Unavailable ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = resourceResolver.getString(R.string.snack_service_unavailable),
											actionLabel = resourceResolver.getString(R.string.retry),
											params = params
										)
									)

								else ->
									sideEffect(
										SignIn.Effect.ShowRetrySnackBar(
											message = resourceResolver.getString(R.string.snack_default_error),
											actionLabel = resourceResolver.getString(R.string.retry),
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