package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.machine.UpdatePasswordMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<UpdatePassword.State>.updatePasswordIdleTransitions(
	machine: UpdatePasswordMachine,
	host: MachineHost<UpdatePassword.Effect>
) {
	from<UpdatePassword.State.Idle> {
		on<UpdatePassword.Action.SetPassword> { state, action ->
			state.copy(password = action.password)
		}

		on<UpdatePassword.Action.TogglePasswordVisibility> { state, _ ->
			state.copy(isPasswordVisible = !state.isPasswordVisible)
		}

		onTo<UpdatePassword.Action.ClickSignIn, UpdatePassword.State.Updating> { state, _ ->
			machine.startUpdate(host = host, state = state)
		}
	}
}
