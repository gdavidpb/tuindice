package com.gdavidpb.tuindice.about.presentation.transition

import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.machine.SettingsInternalEvent
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<Settings.State>.settingsAnyStateTransitions(
	machine: SettingsMachine,
	host: MachineHost<Settings.Effect>
) {
	settingsLoadTransitions(machine = machine, host = host)
	settingsNavigationTransitions(machine = machine, host = host)
	settingsAssistantTransitions(machine = machine, host = host)
	settingsHandOffTransitions(machine = machine, host = host)
}

private fun MachineDefinitionBuilder<Settings.State>.settingsLoadTransitions(
	machine: SettingsMachine,
	host: MachineHost<Settings.Effect>
) {
	fromAny {
		on<Settings.Action.LoadSettings> { state, _ ->
			machine.loadVersion(host = host)
			state
		}

		onTo<SettingsInternalEvent.SettingsVersionLoaded, Settings.State.Content> { _, event ->
			// The observation starts with the content it feeds. LoadSettings is the initial
			// action, so this runs once per screen.
			machine.startObservation(host = host)

			Settings.State.Content(
				identity = null,
				versionText = event.versionText,
				usageDataCollectionEnabled = event.usageDataCollectionEnabled
			)
		}

		// A failed version read must not leave the screen blank: fall back to Content
		// so the support, legal and sign-out entry points stay reachable.
		onTo<SettingsInternalEvent.SettingsVersionLoadFailed, Settings.State.Content> { _, event ->
			machine.startObservation(host = host)

			Settings.State.Content(
				identity = null,
				versionText = event.versionFallbackText,
				usageDataCollectionEnabled = event.usageDataCollectionEnabled
			)
		}
	}
}

private fun MachineDefinitionBuilder<Settings.State>.settingsNavigationTransitions(
	machine: SettingsMachine,
	host: MachineHost<Settings.Effect>
) {
	fromAny {
		on<Settings.Action.OpenTermsAndConditions>(
			emits = setOf(Settings.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openTermsAndConditions(host = host)
			state
		}

		on<Settings.Action.OpenPrivacyPolicy>(
			emits = setOf(Settings.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openPrivacyPolicy(host = host)
			state
		}

		on<Settings.Action.OpenSupport>(
			emits = setOf(Settings.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openSupport(host = host)
			state
		}

		on<Settings.Action.OpenAbout>(
			emits = setOf(Settings.Effect.NavigateToAbout::class)
		) { state, _ ->
			host.sendEffect(Settings.Effect.NavigateToAbout)
			state
		}

		// The host owns the flow (it checks the pending changes and asks for the confirmation), so the row only asks.
		on<Settings.Action.RequestSignOut>(
			emits = setOf(Settings.Effect.RequestSignOut::class)
		) { state, _ ->
			host.sendEffect(Settings.Effect.RequestSignOut)
			state
		}
	}
}

private fun MachineDefinitionBuilder<Settings.State>.settingsAssistantTransitions(
	machine: SettingsMachine,
	host: MachineHost<Settings.Effect>
) {
	fromAny {
		on<Settings.Action.OpenAssistantMemory>(
			emits = setOf(Settings.Effect.NavigateToAssistantMemory::class)
		) { state, _ ->
			host.sendEffect(Settings.Effect.NavigateToAssistantMemory)
			state
		}

		// The row only asks: the confirmation is a destination of its own, and it answers with the deletion.
		on<Settings.Action.RequestDeleteAssistantConversations>(
			emits = setOf(Settings.Effect.NavigateToDeleteAssistantConversationsConfirmation::class)
		) { state, _ ->
			host.sendEffect(Settings.Effect.NavigateToDeleteAssistantConversationsConfirmation)
			state
		}

		on<Settings.Action.ConfirmDeleteAssistantConversations> { state, _ ->
			machine.deleteAssistantConversations(host = host)
			state
		}

		on<SettingsInternalEvent.AssistantConversationsDeleted>(
			emits = setOf(Settings.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Settings.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<SettingsInternalEvent.AssistantConversationsDeleteFailed>(
			emits = setOf(Settings.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Settings.Effect.ShowSnackBar(message = event.message))
			state
		}
	}
}

private fun MachineDefinitionBuilder<Settings.State>.settingsHandOffTransitions(
	machine: SettingsMachine,
	host: MachineHost<Settings.Effect>
) {
	fromAny {
		on<Settings.Action.ShareApp>(
			emits = setOf(Settings.Effect.ShareText::class)
		) { state, _ ->
			machine.shareApp(host = host)
			state
		}

		on<Settings.Action.OpenUrl> { state, action ->
			machine.openExternalUrl(host = host, url = action.url)
			state
		}

		on<Settings.Action.RateOnStore> { state, _ ->
			machine.openStore(host = host)
			state
		}

		on<Settings.Action.ReportBug> { state, _ ->
			machine.contactSupport(host = host)
			state
		}

		on<SettingsInternalEvent.SupportEmailUriLoaded>(
			emits = setOf(Settings.Effect.OpenUri::class)
		) { state, event ->
			host.sendEffect(Settings.Effect.OpenUri(uri = event.uri))
			state
		}

		on<SettingsInternalEvent.StoreUriLoaded>(
			emits = setOf(Settings.Effect.OpenUri::class)
		) { state, event ->
			host.sendEffect(Settings.Effect.OpenUri(uri = event.uri))
			state
		}
	}
}
