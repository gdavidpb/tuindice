package com.gdavidpb.tuindice.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.machine.MainInternalEvent

internal fun MachineDefinitionBuilder<Main.State>.mainContentTransitions(
	host: MachineHost<Main.Effect>
) {
	from<Main.State.Content> {
		// The wizard starts at most once per session: the EFSM guard over the
		// requested flag keeps re-approvals silent, as the old reducer did.
		on<MainInternalEvent.WizardStartApproved>(
			emits = setOf(Main.Effect.NavigateToWizard::class)
		) { state, _ ->
			if (state.wizardStartRequested) {
				state
			} else {
				host.sendEffect(Main.Effect.NavigateToWizard)

				state.copy(wizardStartRequested = true)
			}
		}
	}
}
