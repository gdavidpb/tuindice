package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignOut.State>.signOutLoggingOutTransitions(
	machine: SignOutMachine,
	host: MachineHost<SignOut.Effect>
) {
	from<SignOut.State.LoggingOut> {
		// Bug-compatible re-entry: clicking again while logging out re-runs the
		// confirmation, exactly as the old dispatcher did.
		on<SignOut.Action.ClickSignOut> { state, _ ->
			machine.confirmSignOut(host = host)
			state
		}
	}
}
