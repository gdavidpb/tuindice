package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignOut.State>.signOutFlushFailedTransitions(
	machine: SignOutMachine,
	host: MachineHost<SignOut.Effect>
) {
	from<SignOut.State.FlushFailed> {
		// The decision the view model used to make lives in the table: with outdated
		// credentials the primary action escalates to the password update; otherwise
		// it retries the flush with the pending changes S already holds.
		on<SignOut.Action.ClickSignOut>(
			emits = setOf(SignOut.Effect.NavigateToUpdatePassword::class)
		) { state, _ ->
			if (state.requiresPasswordUpdate) {
				host.sendEffect(SignOut.Effect.NavigateToUpdatePassword)
			} else {
				machine.flushAndSignOut(host = host, pendingChanges = state.pendingChanges)
			}

			state
		}

		on<SignOut.Action.ForceSignOut> { state, _ ->
			machine.forceSignOut(
				host = host,
				pendingChanges = state.pendingChanges,
				requiresPasswordUpdate = state.requiresPasswordUpdate
			)
			state
		}
	}
}
