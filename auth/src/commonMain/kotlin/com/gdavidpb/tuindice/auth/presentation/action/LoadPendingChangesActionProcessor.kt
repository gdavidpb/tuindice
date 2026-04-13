package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.domain.usecase.LoadPendingChangesUseCase
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_default_error

class LoadPendingChangesActionProcessor(
	private val loadPendingChangesUseCase: LoadPendingChangesUseCase
) : ActionProcessor<SignOut.State, SignOut.Action.LoadPendingChanges, SignOut.Effect>() {
	override suspend fun process(
		action: SignOut.Action.LoadPendingChanges,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return loadPendingChangesUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state: SignOut.State ->
						state
					}

					is UseCaseState.Data -> suspend { _: SignOut.State ->
						if (useCaseState.value.totalCount == 0) {
							SignOut.State.Plain
						} else {
							SignOut.State.Pending(useCaseState.value)
						}
					}

					is UseCaseState.Error -> suspend { state: SignOut.State ->
						sideEffect(
							SignOut.Effect.ShowSnackBar(
								message = getString(Res.string.snack_default_error)
							)
						)
						state
					}
				}
			}
	}
}
