package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ToggleUpdatePasswordVisibilityActionProcessor :
	ActionProcessor<UpdatePassword.State, UpdatePassword.Action.TogglePasswordVisibility, UpdatePassword.Effect>() {
	override suspend fun process(
		action: UpdatePassword.Action.TogglePasswordVisibility,
		sideEffect: (UpdatePassword.Effect) -> Unit
	): Flow<Mutation<UpdatePassword.State>> {
		return flowOf { state ->
			if (state is UpdatePassword.State.Idle) {
				state.copy(
					isPasswordVisible = !state.isPasswordVisible
				)
			} else {
				state
			}
		}
	}
}
