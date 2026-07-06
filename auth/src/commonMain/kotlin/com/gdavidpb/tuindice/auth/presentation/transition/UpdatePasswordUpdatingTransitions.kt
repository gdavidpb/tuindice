package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.machine.UpdatePasswordInternalEvent
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<UpdatePassword.State>.updatePasswordUpdatingTransitions(
	host: MachineHost<UpdatePassword.Effect>
) {
	from<UpdatePassword.State.Updating> {
		on<UpdatePasswordInternalEvent.PasswordUpdateSucceeded>(
			emits = setOf(UpdatePassword.Effect.PasswordUpdated::class)
		) { state, event ->
			host.sendEffect(UpdatePassword.Effect.PasswordUpdated(message = event.message))
			state
		}

		// Failure restores Idle with the typed password and the error inline; the
		// dialog stays open so the user can correct the password in place.
		onTo<UpdatePasswordInternalEvent.PasswordUpdateFailed, UpdatePassword.State.Idle> { state, event ->
			UpdatePassword.State.Idle(
				password = state.password,
				error = event.message
			)
		}
	}
}
