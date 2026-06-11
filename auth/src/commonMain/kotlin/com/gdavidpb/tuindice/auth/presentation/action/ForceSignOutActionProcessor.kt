package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_default_error

class ForceSignOutActionProcessor(
	private val signOutUseCase: SignOutUseCase
) : ActionProcessor<SignOut.State, SignOut.Action.ForceSignOut, SignOut.Effect> {
	override suspend fun process(
		action: SignOut.Action.ForceSignOut,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return signOutUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { _: SignOut.State ->
						SignOut.State.LoggingOut(
							pendingChanges = action.pendingChanges,
							requiresPasswordUpdate = action.requiresPasswordUpdate
						)
					}

					is UseCaseState.Data -> suspend { state ->
						sideEffect(SignOut.Effect.NavigateToSignIn)
						state
					}

					is UseCaseState.Error -> suspend { _: SignOut.State ->
						sideEffect(
							SignOut.Effect.ShowSnackBar(
								message = getString(Res.string.snack_default_error)
							)
						)
						SignOut.State.FlushFailed(
							pendingChanges = action.pendingChanges,
							requiresPasswordUpdate = action.requiresPasswordUpdate
						)
					}
				}
			}
	}
}
