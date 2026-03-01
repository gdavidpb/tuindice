package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.error_invalid_password
import tuindice.login.generated.resources.snack_default_error
import tuindice.login.generated.resources.snack_network_unavailable
import tuindice.login.generated.resources.snack_password_updated
import tuindice.login.generated.resources.snack_service_unavailable
import tuindice.login.generated.resources.snack_timeout

class UpdatePasswordActionProcessor(
	private val updatePasswordUseCase: UpdatePasswordUseCase
) : ActionProcessor<UpdatePassword.State, UpdatePassword.Action.ClickSignIn, UpdatePassword.Effect>() {

	override suspend fun process(
		action: UpdatePassword.Action.ClickSignIn,
		sideEffect: (UpdatePassword.Effect) -> Unit
	): Flow<Mutation<UpdatePassword.State>> {
		return updatePasswordUseCase.execute(params = action.password)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						if (state is UpdatePassword.State.Idle)
							UpdatePassword.State.Updating(
								password = action.password
							)
						else
							state
					}

					is UseCaseState.Data -> run {
						val successMessage = getString(Res.string.snack_password_updated)

						suspend { state: UpdatePassword.State ->
							sideEffect(
								UpdatePassword.Effect.ShowSnackBar(
									message = successMessage
								)
							)

							state
						}
					}

					is UseCaseState.Error -> run {
						val error = when (val useCaseError = useCaseState.error) {
							is SignInUseCaseError.InvalidCredentials ->
								getString(Res.string.error_invalid_password)

							is SignInUseCaseError.NoConnection ->
								if (useCaseError.isNetworkAvailable)
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

						suspend { state: UpdatePassword.State ->
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
}
