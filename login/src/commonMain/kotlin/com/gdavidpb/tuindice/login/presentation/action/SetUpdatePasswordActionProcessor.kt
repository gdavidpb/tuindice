package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetUpdatePasswordActionProcessor :
	ActionProcessor<UpdatePassword.State, UpdatePassword.Action.SetPassword, UpdatePassword.Effect>() {
	override fun process(
		action: UpdatePassword.Action.SetPassword,
		sideEffect: (UpdatePassword.Effect) -> Unit
	): Flow<Mutation<UpdatePassword.State>> {
		return flowOf { state ->
			if (state is UpdatePassword.State.Idle)
				state.copy(
					password = action.password
				)
			else
				state
		}
	}
}