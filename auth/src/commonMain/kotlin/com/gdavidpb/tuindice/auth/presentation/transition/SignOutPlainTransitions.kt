package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignOut.State>.signOutPlainTransitions(
	machine: SignOutMachine,
	host: MachineHost<SignOut.Effect>
) {
	from<SignOut.State.Plain> {
		// EFSM guard over the payload: route-resolved pending changes flush first;
		// otherwise a plain confirmation runs. State changes arrive as internal events.
		on<SignOut.Action.ClickSignOut> { state, action ->
			val resolved = action.resolvedPendingChanges

			if (resolved != null && resolved.totalCount > 0) {
				machine.flushAndSignOut(host = host, pendingChanges = resolved)
			} else {
				machine.confirmSignOut(host = host)
			}

			state
		}
	}
}
