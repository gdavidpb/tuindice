package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignOut.State>.signOutPendingTransitions(
	machine: SignOutMachine,
	host: MachineHost<SignOut.Effect>
) {
	from<SignOut.State.Pending> {
		on<SignOut.Action.ClickSignOut> { state, _ ->
			machine.flushAndSignOut(host = host, pendingChanges = state.pendingChanges)
			state
		}
	}
}
