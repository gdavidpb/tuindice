package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.auth.utils.extension.isUsbId
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

		on<SignIn.Action.ToggleIdentifierMode> { state, _ ->
			val nextMode = when (state.identifierMode) {
				SignInIdentifierMode.UsbId -> SignInIdentifierMode.UsbEmail
				SignInIdentifierMode.UsbEmail -> SignInIdentifierMode.UsbId
			}
			val nextUsbId = when {
				nextMode == SignInIdentifierMode.UsbId && !state.usbId.isUsbId() -> ""
				else -> state.usbId
			}

			state.copy(
				usbId = nextUsbId,
				identifierMode = nextMode
			)
		}

		onTo<SignIn.Action.ClickSignIn, SignIn.State.LoggingIn> { state, _ ->
			machine.startSignIn(host = host, state = state)
		}
	}
}
