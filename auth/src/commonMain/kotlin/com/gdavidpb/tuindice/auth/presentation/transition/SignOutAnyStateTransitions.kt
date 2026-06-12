package com.gdavidpb.tuindice.auth.presentation.transition

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutInternalEvent
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<SignOut.State>.signOutAnyStateTransitions(
	machine: SignOutMachine,
	host: MachineHost<SignOut.Effect>
) {
	fromAny {
		on<SignOut.Action.Initialize> { state, action ->
			machine.initialize(host = host, pendingChanges = action.pendingChanges)
			state
		}

		// Triggered by the navigation result after a successful password update: the
		// stale requiresPasswordUpdate branch must not re-open the dialog, so this is
		// its own symbol that flushes the nav-provided pending changes directly.
		on<SignOut.Action.RetryFlushAndSignOut> { state, action ->
			machine.flushAndSignOut(host = host, pendingChanges = action.pendingChanges)
			state
		}

		onTo<SignOutInternalEvent.SignOutInitializedPlain, SignOut.State.Plain> { _, _ ->
			SignOut.State.Plain
		}

		onTo<SignOutInternalEvent.SignOutInitializedPending, SignOut.State.Pending> { _, event ->
			SignOut.State.Pending(pendingChanges = event.pendingChanges)
		}

		onTo<SignOutInternalEvent.LoggingOutObserved, SignOut.State.LoggingOut> { _, event ->
			SignOut.State.LoggingOut(
				pendingChanges = event.pendingChanges,
				requiresPasswordUpdate = event.requiresPasswordUpdate
			)
		}

		onTo<SignOutInternalEvent.PendingChangesFound, SignOut.State.Pending> { _, event ->
			SignOut.State.Pending(pendingChanges = event.pendingChanges)
		}

		on<SignOutInternalEvent.SignOutSucceeded>(
			emits = setOf(SignOut.Effect.NavigateToSignIn::class)
		) { state, _ ->
			host.sendEffect(SignOut.Effect.NavigateToSignIn)
			state
		}

		onTo<SignOutInternalEvent.SignOutFailedToPlain, SignOut.State.Plain>(
			emits = setOf(SignOut.Effect.ShowSnackBar::class)
		) { _, event ->
			host.sendEffect(SignOut.Effect.ShowSnackBar(message = event.message))

			SignOut.State.Plain
		}

		onTo<SignOutInternalEvent.FlushFailedObserved, SignOut.State.FlushFailed>(
			emits = setOf(SignOut.Effect.ShowSnackBar::class)
		) { _, event ->
			if (event.message != null) {
				host.sendEffect(SignOut.Effect.ShowSnackBar(message = event.message))
			}

			SignOut.State.FlushFailed(
				pendingChanges = event.pendingChanges,
				requiresPasswordUpdate = event.requiresPasswordUpdate
			)
		}
	}
}
