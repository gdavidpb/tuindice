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

		// The assistant rows render with content only, like the usage toggle. The switch moves at once, and the write
		// is queued right here, so the writes keep the order of the moves.
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

		// The store's consent reaches the switch only while no consent write is queued or running (see
		// SettingsMachine.startObservation), so the switch takes the value the event carries. A move made after the
		// queue drained and before this event arrived has queued a write already: the switch keeps that move, and the
		// store's value comes again once that write ends.
		on<SettingsInternalEvent.AssistantConsentObserved> { state, event ->
			if (machine.assistantConsentWritesPending()) state else state.copy(assistantConsentGranted = event.granted)
		}

		// A failed write is told to the student. The switch goes back to the store's value once the queue has drained.
		on<SettingsInternalEvent.AssistantConsentWriteFailed>(
			emits = setOf(Settings.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Settings.Effect.ShowSnackBar(message = event.message))

			state
		}
	}
}
