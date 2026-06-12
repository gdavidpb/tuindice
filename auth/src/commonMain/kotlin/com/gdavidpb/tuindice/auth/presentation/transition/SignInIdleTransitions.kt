package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignIn.State>.idleTransitions(
	machine: SignInMachine,
	host: MachineHost<SignIn.Effect>
) {
	from<SignIn.State.Idle> {
		on<SignIn.Action.SetUsbId> { state, action ->
			state.copy(usbId = action.usbId)
		}

		on<SignIn.Action.SetPassword> { state, action ->
			state.copy(password = action.password)
		}

		on<SignIn.Action.TogglePasswordVisibility> { state, _ ->
			state.copy(isPasswordVisible = !state.isPasswordVisible)
		}

		onTo<SignIn.Action.ClickSignIn, SignIn.State.LoggingIn> { state, action ->
			machine.startSignIn(host = host, state = state, action = action)
		}
	}
}
