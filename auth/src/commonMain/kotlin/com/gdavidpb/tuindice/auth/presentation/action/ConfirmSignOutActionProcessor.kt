package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_default_error

class ConfirmSignOutActionProcessor(
	private val confirmSignOutUseCase: ConfirmSignOutUseCase,
	private val signOutUseCase: SignOutUseCase
) : ActionProcessor<SignOut.State, SignOut.Action.ConfirmSignOut, SignOut.Effect>() {
	override suspend fun process(
		action: SignOut.Action.ConfirmSignOut,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return flow {
			confirmSignOutUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						emit(
							suspend { _: SignOut.State ->
								SignOut.State.LoggingOut()
							}
						)

					is UseCaseState.Data ->
						if (useCaseState.value.totalCount > 0) {
							emit(
								suspend { _: SignOut.State ->
									SignOut.State.Pending(useCaseState.value)
								}
							)
						} else {
							signOutUseCase.execute(Unit).collect { signOutState ->
								when (signOutState) {
									is UseCaseState.Loading ->
										emit(
											suspend { _: SignOut.State ->
												SignOut.State.LoggingOut()
											}
										)

									is UseCaseState.Data ->
										emit(
											suspend { state ->
												sideEffect(SignOut.Effect.NavigateToSignIn)
												state
											}
										)

									is UseCaseState.Error ->
										emit(
											suspend { _: SignOut.State ->
												sideEffect(
													SignOut.Effect.ShowSnackBar(
														message = getString(Res.string.snack_default_error)
													)
												)
												SignOut.State.Plain
											}
										)
								}
							}
						}

					is UseCaseState.Error ->
						emit(
							suspend { _: SignOut.State ->
								sideEffect(
									SignOut.Effect.ShowSnackBar(
										message = getString(Res.string.snack_default_error)
									)
								)
								SignOut.State.Plain
							}
						)
				}
			}
		}
	}
}
