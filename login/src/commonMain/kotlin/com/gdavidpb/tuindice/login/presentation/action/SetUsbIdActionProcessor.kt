package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetUsbIdActionProcessor :
	ActionProcessor<SignIn.State, SignIn.Action.SetUsbId, SignIn.Effect>() {
	override fun process(
		action: SignIn.Action.SetUsbId,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		return flowOf { state ->
			if (state is SignIn.State.Idle)
				state.copy(
					usbId = action.usbId
				)
			else
				state
		}
	}
}