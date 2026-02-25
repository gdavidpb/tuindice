package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UpdatePasswordActionProcessor(
	private val updatePasswordUseCase: UpdatePasswordUseCase,
	private val textProvider: LoginTextProvider
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
								message = textProvider.passwordUpdated()
							)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						val error = when (val useCaseError = useCaseState.error) {
							is SignInUseCaseError.InvalidCredentials ->
								textProvider.invalidPassword()

							is SignInUseCaseError.NoConnection ->
								if (useCaseError.isNetworkAvailable)
									textProvider.serviceUnavailable()
								else
									textProvider.networkUnavailable()

							is SignInUseCaseError.Timeout ->
								textProvider.timeout()

							is SignInUseCaseError.Unavailable ->
								textProvider.serviceUnavailable()

							else ->
								textProvider.defaultError()
						}

						val currentPassword = when (state) {
							is UpdatePassword.State.Idle -> state.password
							is UpdatePassword.State.Updating -> state.password
						}

						sideEffect(
							UpdatePassword.Effect.ShowSnackBar(
								message = error
							)
						)

						UpdatePassword.State.Idle(
							password = currentPassword,
							error = error
						)
					}
				}
			}
	}
}
