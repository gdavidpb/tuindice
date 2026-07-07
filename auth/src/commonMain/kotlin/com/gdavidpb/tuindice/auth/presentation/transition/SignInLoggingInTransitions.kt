package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInInternalEvent
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignIn.State>.loggingInTransitions(
	machine: SignInMachine,
	host: MachineHost<SignIn.Effect>
) {
	from<SignIn.State.LoggingIn> {
		onTo<SignIn.Action.ClickCancelSignIn, SignIn.State.Idle> { state, _ ->
			machine.cancelSignIn(state = state)
		}

		on<SignInInternalEvent.SignInSucceeded>(
			emits = setOf(SignIn.Effect.NavigateToSummary::class)
		) { state, _ ->
			host.sendEffect(SignIn.Effect.NavigateToSummary)
			state
		}

		on<SignInInternalEvent.OutdatedAppDetected>(
			emits = setOf(SignIn.Effect.ShowOutdatedApp::class)
		) { state, _ ->
			machine.showOutdatedApp(host = host)
			state
		}

		onTo<SignInInternalEvent.SignInFailed, SignIn.State.Idle>(
			emits = setOf(
				SignIn.Effect.ShowSnackBar::class,
				SignIn.Effect.ShowRetrySnackBar::class
			)
		) { state, event ->
			machine.failSignIn(host = host, state = state, event = event)
		}
	}
}
