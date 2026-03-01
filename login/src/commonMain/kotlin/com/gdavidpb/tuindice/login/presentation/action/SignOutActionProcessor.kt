package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.snack_default_error

class SignOutActionProcessor(
	private val signOutUseCase: SignOutUseCase
) : ActionProcessor<SignOut.State, SignOut.Action.ConfirmSignOut, SignOut.Effect>() {
	override suspend fun process(
		action: SignOut.Action.ConfirmSignOut,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return signOutUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						SignOut.State.LoggingOut
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							SignOut.Effect.NavigateToSignIn
						)

						state
					}

					is UseCaseState.Error -> run {
						val errorMessage = getString(Res.string.snack_default_error)

						suspend { _: SignOut.State ->
							sideEffect(
								SignOut.Effect.ShowSnackBar(
									message = errorMessage
								)
							)

							SignOut.State.Idle
						}
					}
				}
			}
	}
}
