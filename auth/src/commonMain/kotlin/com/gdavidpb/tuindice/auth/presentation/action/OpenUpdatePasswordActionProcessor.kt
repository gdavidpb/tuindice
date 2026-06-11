package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class OpenUpdatePasswordActionProcessor :
	ActionProcessor<SignOut.State, SignOut.Action.OpenUpdatePassword, SignOut.Effect> {
	override suspend fun process(
		action: SignOut.Action.OpenUpdatePassword,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return flowOf(
			suspend { state: SignOut.State ->
				sideEffect(SignOut.Effect.NavigateToUpdatePassword)
				state
			}
		)
	}
}
