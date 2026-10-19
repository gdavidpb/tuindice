package com.gdavidpb.tuindice.about.presentation.transition

import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.machine.SettingsInternalEvent
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<Settings.State>.settingsContentTransitions(
	machine: SettingsMachine,
	host: MachineHost<Settings.Effect>
) {
	from<Settings.State.Content> {
		// The consent toggle only renders with content on screen; persisting and the
		// state copy happen in the same row.
		on<Settings.Action.SetUsageDataCollectionEnabled> { state, action ->
			machine.persistUsageDataCollection(enabled = action.enabled)

			state.copy(usageDataCollectionEnabled = action.enabled)
		}

		// The assistant rows render with content only, like the usage toggle. The switch
		// moves at once and the write runs in the command; the observed value then
		// converges to what the store holds.
		on<Settings.Action.SetAssistantConsent> { state, action ->
			machine.setAssistantConsent(host = host, granted = action.granted)

			state.copy(assistantConsentGranted = action.granted)
		}

		on<SettingsInternalEvent.ProfileIdentityObserved> { state, event ->
			state.copy(identity = event.identity)
		}

		on<SettingsInternalEvent.AssistantAvailabilityObserved> { state, event ->
			state.copy(assistantAvailable = event.visible)
		}

		on<SettingsInternalEvent.AssistantConsentObserved> { state, event ->
			state.copy(assistantConsentGranted = event.granted)
		}

		// A failed write puts the switch back where the store has it and says so.
		on<SettingsInternalEvent.AssistantConsentWriteFailed>(
			emits = setOf(Settings.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Settings.Effect.ShowSnackBar(message = event.message))

			state.copy(assistantConsentGranted = event.restoredGranted)
		}
	}
}
