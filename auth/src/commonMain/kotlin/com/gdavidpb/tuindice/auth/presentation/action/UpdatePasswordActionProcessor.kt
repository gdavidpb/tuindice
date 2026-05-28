package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_password
import tuindice.auth.generated.resources.error_untrusted
import tuindice.auth.generated.resources.snack_default_error
import tuindice.auth.generated.resources.snack_network_unavailable
import tuindice.auth.generated.resources.snack_password_updated
import tuindice.auth.generated.resources.snack_service_unavailable
import tuindice.auth.generated.resources.snack_timeout
import tuindice.auth.generated.resources.snack_update_password_failed

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
					is UseCaseState.Loading -> suspend { state ->
						if (state is UpdatePassword.State.Idle)
							UpdatePassword.State.Updating(
								password = action.password,
								isPasswordVisible = state.isPasswordVisible
							)
						else
							state
					}

					is UseCaseState.Data -> suspend { state: UpdatePassword.State ->
						val successMessage = getString(Res.string.snack_password_updated)

						sideEffect(
							UpdatePassword.Effect.PasswordUpdated(
								message = successMessage
							)
						)

						state
					}

					is UseCaseState.Error -> suspend { state: UpdatePassword.State ->
						val error = when (val useCaseError = useCaseState.error) {
							is SignInUseCaseError.InvalidCredentials ->
								getString(Res.string.error_invalid_password)

							is SignInUseCaseError.AccountDisabled ->
								getString(Res.string.error_account_disabled)

							is SignInUseCaseError.Untrusted ->
								getString(Res.string.error_untrusted)

							is SignInUseCaseError.AuthenticationFailed ->
								getString(Res.string.snack_update_password_failed)

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
