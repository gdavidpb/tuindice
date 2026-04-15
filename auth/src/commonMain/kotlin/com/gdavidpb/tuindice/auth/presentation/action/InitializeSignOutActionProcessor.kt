package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InitializeSignOutActionProcessor :
	ActionProcessor<SignOut.State, SignOut.Action.Initialize, SignOut.Effect>() {
	override suspend fun process(
		action: SignOut.Action.Initialize,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return flowOf(
			suspend { _: SignOut.State ->
				if (action.pendingChanges.totalCount == 0) {
					SignOut.State.Plain
				} else {
					SignOut.State.Pending(action.pendingChanges)
				}
			}
		)
	}
}
