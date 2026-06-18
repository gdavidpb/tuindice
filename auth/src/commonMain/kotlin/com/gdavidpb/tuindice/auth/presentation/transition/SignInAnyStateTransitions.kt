package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignIn.State>.anyStateTransitions(
	machine: SignInMachine,
	host: MachineHost<SignIn.Effect>
) {
	fromAny {
		on<SignIn.Action.ClickTermsAndConditions>(
			emits = setOf(SignIn.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openTermsAndConditions(host = host)
			state
		}

		on<SignIn.Action.ClickPrivacyPolicy>(
			emits = setOf(SignIn.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openPrivacyPolicy(host = host)
			state
		}

		on<SignIn.Action.ClickUpdateApp>(
			emits = setOf(SignIn.Effect.TriggerUpdateFlow::class)
		) { state, _ ->
			machine.requestUpdate(host = host)
			state
		}

		on<SignIn.Action.SetUsageDataCollectionEnabled> { state, action ->
			machine.persistUsageDataCollection(enabled = action.enabled)

			state.withUsageDataCollection(enabled = action.enabled)
		}
	}
}

private fun SignIn.State.withUsageDataCollection(enabled: Boolean): SignIn.State {
	return when (this) {
		is SignIn.State.Idle -> copy(usageDataCollectionEnabled = enabled)
		is SignIn.State.LoggingIn -> copy(usageDataCollectionEnabled = enabled)
		is SignIn.State.OutdatedApp -> this
	}
}
