package com.gdavidpb.tuindice.presentation.transition

import com.gdavidpb.tuindice.base.domain.model.UpdateAction
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
			machine.startUp(host = host)
			state
		}

		onTo<Main.Action.ShowOutdatedApp, Main.State.OutdatedApp> { _, action ->
			Main.State.OutdatedApp(outdatedAppState = action.outdatedAppState)
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

		on<Main.Action.RequestSync> { state, _ ->
			machine.requestSync(host = host)
			state
		}

		on<Main.Action.SetLastMainSection> { state, action ->
			machine.setLastMainSection(host = host, section = action.section)
			state
		}

		on<Main.Action.RequestWizardStart> { state, _ ->
			machine.requestWizardStart(host = host)
			state
		}

		onTo<MainInternalEvent.StartUpStarting, Main.State.Starting> { _, _ ->
			Main.State.Starting
		}

		onTo<MainInternalEvent.StartUpCompleted, Main.State.Content> { _, event ->
			Main.State.Content(startDestination = event.startDestination)
		}

		onTo<MainInternalEvent.AppUnavailableResolved, Main.State.AppUnavailable> { _, event ->
			Main.State.AppUnavailable(notice = event.notice)
		}

		onTo<MainInternalEvent.OutdatedAppResolved, Main.State.OutdatedApp> { _, event ->
			Main.State.OutdatedApp(outdatedAppState = event.outdatedAppState)
		}

		onTo<MainInternalEvent.StartUpFailed, Main.State.Failed>(
			emits = setOf(Main.Effect.NavigateToGooglePlayServicesUnavailableDialog::class)
		) { _, event ->
			if (event.noServices) {
				host.sendEffect(Main.Effect.NavigateToGooglePlayServicesUnavailableDialog)
			}

			Main.State.Failed
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
