package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TogglePasswordVisibilityActionProcessor :
	ActionProcessor<SignIn.State, SignIn.Action.TogglePasswordVisibility, SignIn.Effect>() {
	override suspend fun process(
		action: SignIn.Action.TogglePasswordVisibility,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		return flowOf { state ->
			if (state is SignIn.State.Idle) {
				state.copy(
					isPasswordVisible = !state.isPasswordVisible
				)
			} else {
				state
			}
		}
	}
}
