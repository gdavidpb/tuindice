package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UpdatePasswordActionProcessor(
	private val updatePasswordUseCase: UpdatePasswordUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<UpdatePassword.State, UpdatePassword.Action.ClickSignIn, UpdatePassword.Effect>() {

	override fun process(
		action: UpdatePassword.Action.ClickSignIn,
		sideEffect: (UpdatePassword.Effect) -> Unit
	): Flow<Mutation<UpdatePassword.State>> {
		return updatePasswordUseCase.execute(params = action.password)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						if (state is UpdatePassword.State.Idle)
							UpdatePassword.State.Updating(
								password = state.password
							)
						else
							state
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							UpdatePassword.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_password_updated)
							)
						)

						sideEffect(
							UpdatePassword.Effect.CloseDialog
						)

						state
					}

					is UseCaseState.Error -> { state ->
						if (state is UpdatePassword.State.Idle) {
							val error = when (val error = useCaseState.error) {
								is SignInError.InvalidCredentials ->
									resourceResolver.getString(R.string.error_invalid_password)

								is SignInError.NoConnection ->
									if (error.isNetworkAvailable)
										resourceResolver.getString(R.string.snack_service_unavailable)
									else
										resourceResolver.getString(R.string.snack_network_unavailable)

								is SignInError.Timeout ->
									resourceResolver.getString(R.string.snack_timeout)

								is SignInError.Unavailable ->
									resourceResolver.getString(R.string.snack_service_unavailable)

								else ->
									resourceResolver.getString(R.string.snack_default_error)
							}

							UpdatePassword.State.Idle(
								password = state.password,
								error = error
							)
						} else
							state
					}
				}
			}
	}
}