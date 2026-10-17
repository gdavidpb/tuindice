package com.gdavidpb.tuindice.about.presentation.transition

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutInternalEvent
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<About.State>.aboutContentTransitions(
	machine: AboutMachine,
	host: MachineHost<About.Effect>
) {
	from<About.State.Content> {
		// The consent toggle only renders with content on screen; persisting and the
		// state copy happen in the same row.
		on<About.Action.SetUsageDataCollectionEnabled> { state, action ->
			machine.persistUsageDataCollection(enabled = action.enabled)

			state.copy(usageDataCollectionEnabled = action.enabled)
		}

		// The assistant rows render with content only, like the usage toggle. The switch
		// moves at once and the write runs in the command; the observed value then
		// converges to what the store holds.
		on<About.Action.SetAssistantConsent> { state, action ->
			machine.setAssistantConsent(host = host, granted = action.granted)

			state.copy(assistantConsentGranted = action.granted)
		}

		on<AboutInternalEvent.AssistantAvailabilityObserved> { state, event ->
			state.copy(assistantAvailable = event.visible)
		}

		on<AboutInternalEvent.AssistantConsentObserved> { state, event ->
			state.copy(assistantConsentGranted = event.granted)
		}

		// A failed write puts the switch back where the store has it and says so.
		on<AboutInternalEvent.AssistantConsentWriteFailed>(
			emits = setOf(About.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(About.Effect.ShowSnackBar(message = event.message))

			state.copy(assistantConsentGranted = event.restoredGranted)
		}
	}
}
