package com.gdavidpb.tuindice.presentation.transition

import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.machine.MainInternalEvent
import com.gdavidpb.tuindice.presentation.machine.MainMachine

internal fun MachineDefinitionBuilder<Main.State>.mainAnyStateTransitions(
	machine: MainMachine,
	host: MachineHost<Main.Effect>
) {
	fromAny {
		on<Main.Action.StartUp> { state, _ ->
			machine.observeOutdatedApp(host = host)
			machine.startUp(host = host)
			state
		}

		onTo<Main.Action.ShowOutdatedApp, Main.State.OutdatedApp> { state, action ->
			Main.State.OutdatedApp(
				outdatedAppState = action.outdatedAppState,
				pendingSessionEnd = state.pendingSessionEnd
			)
		}

		on<Main.Action.RequestReview> { state, _ ->
			machine.requestReview(host = host)
			state
		}

		on<Main.Action.RequestUpdateCheck> { state, _ ->
			machine.requestUpdateCheck(host = host)
			state
		}

		on<Main.Action.ClickUpdateApp>(
			emits = setOf(Main.Effect.TriggerUpdateFlow::class)
		) { state, _ ->
			host.sendEffect(Main.Effect.TriggerUpdateFlow(action = UpdateAction.Immediate))
			state
		}

		on<Main.Action.UpdateFlowCompleted>(
			emits = setOf(Main.Effect.OpenUpdateStoreFallback::class)
		) { state, action ->
			if (action.result is UpdateLaunchResult.OpenStoreFallback) {
				host.sendEffect(Main.Effect.OpenUpdateStoreFallback(result = action.result))
			}

			state
		}

		on<Main.Action.RequestSync> { state, _ ->
			machine.requestSync(host = host)
			machine.ensureMessagingSubscribed()
			state
		}

		// Closing the password dialog is remembered only while the status still asks for one:
		// closed for any other reason there is nothing to hold back.
		on<Main.Action.DismissUpdatePassword> { state, _ ->
			if (state is Main.State.Content && state.syncStatus.requiresPassword) {
				state.copy(isUpdatePasswordDismissed = true)
			} else {
				state
			}
		}

		on<Main.Action.RequestSignOut> { state, _ ->
			machine.prepareSignOut(host = host)
			state
		}

		// The route attended the pending end of the session (it took the user to the sign-in and showed
		// the notice, if any): only now is it cleared and the copy of the notice kept for the next start
		// taken. The id of an end that was replaced in the meantime changes nothing: the new one is
		// still owed.
		on<Main.Action.SessionEndHandled> { state, action ->
			val pending = state.pendingSessionEnd

			if (pending != null && pending.id == action.id) {
				machine.finishSessionEnd(host = host, pending = pending)
				state.withPendingSessionEnd(null)
			} else {
				state
			}
		}

		// Telemetry-only rows: the host route dispatches these when the sync status
		// transitions into a degraded state, so backend outages become measurable on the
		// app_action rail. The self-loop transition itself is filtered by the analytics
		// policy — the action event is the signal.
		on<Main.Action.NoteSyncUnavailable> { state, _ ->
			state
		}

		on<Main.Action.NoteSyncFailed> { state, _ ->
			state
		}

		on<Main.Action.SetLastMainSection> { state, action ->
			machine.setLastMainSection(host = host, section = action.section)
			state
		}

		onTo<MainInternalEvent.StartUpStarting, Main.State.Starting> { state, _ ->
			Main.State.Starting(pendingSessionEnd = state.pendingSessionEnd)
		}

		onTo<MainInternalEvent.StartUpCompleted, Main.State.Content> { state, event ->
			machine.observeContent(host = host)

			// The notice the start-up found goes through the rule of the live one: the route attends it
			// and takes it once it showed it.
			val notice = event.sessionNotice

			Main.State.Content(
				startDestination = event.startDestination,
				pendingSessionEnd = if (notice != null) {
					machine.beginSessionEnd(current = state.pendingSessionEnd, notice = notice)
				} else {
					state.pendingSessionEnd
				}
			)
		}

		// The status belongs to the content: before it there is no sync to speak of. A status that
		// no longer asks for the password forgets the dismissal, so the next problem asks again.
		on<MainInternalEvent.SyncStatusObserved> { state, event ->
			if (state is Main.State.Content) {
				state.copy(
					syncStatus = event.syncStatus,
					isUpdatePasswordDismissed = state.isUpdatePasswordDismissed &&
						event.syncStatus.requiresPassword
				)
			} else {
				state
			}
		}

		// Going to the sign-in does not depend on the notice: the end is pending whatever it brings.
		on<MainInternalEvent.SessionInvalidationObserved> { state, event ->
			state.withPendingSessionEnd(
				machine.beginSessionEnd(current = state.pendingSessionEnd, notice = event.sessionNotice)
			)
		}

		on<MainInternalEvent.SignOutPrepared>(
			emits = setOf(Main.Effect.NavigateToSignOutDialog::class)
		) { state, event ->
			host.sendEffect(Main.Effect.NavigateToSignOutDialog(pendingChanges = event.pendingChanges))
			state
		}

		on<MainInternalEvent.SignOutPreparationFailed>(
			emits = setOf(Main.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Main.Effect.ShowSnackBar(message = event.message))
			state
		}

		onTo<MainInternalEvent.AppUnavailableResolved, Main.State.AppUnavailable> { state, event ->
			Main.State.AppUnavailable(notice = event.notice, pendingSessionEnd = state.pendingSessionEnd)
		}

		onTo<MainInternalEvent.OutdatedAppResolved, Main.State.OutdatedApp> { state, event ->
			Main.State.OutdatedApp(
				outdatedAppState = event.outdatedAppState,
				pendingSessionEnd = state.pendingSessionEnd
			)
		}

		onTo<MainInternalEvent.OutdatedAppObserved, Main.State.OutdatedApp> { state, event ->
			Main.State.OutdatedApp(
				outdatedAppState = event.outdatedAppState,
				pendingSessionEnd = state.pendingSessionEnd
			)
		}

		onTo<MainInternalEvent.StartUpFailed, Main.State.Failed>(
			emits = setOf(Main.Effect.NavigateToGooglePlayServicesUnavailableDialog::class)
		) { state, event ->
			if (event.noServices) {
				host.sendEffect(Main.Effect.NavigateToGooglePlayServicesUnavailableDialog)
			}

			Main.State.Failed(pendingSessionEnd = state.pendingSessionEnd)
		}

		on<MainInternalEvent.ReviewRequested>(
			emits = setOf(Main.Effect.TriggerReviewFlow::class)
		) { state, _ ->
			host.sendEffect(Main.Effect.TriggerReviewFlow)
			state
		}

		on<MainInternalEvent.UpdateInfoLoaded>(
			emits = setOf(Main.Effect.TriggerUpdateFlow::class)
		) { state, event ->
			host.sendEffect(Main.Effect.TriggerUpdateFlow(action = event.action))
			state
		}
	}
}
