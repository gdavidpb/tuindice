package com.gdavidpb.tuindice.about.presentation.transition

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder

internal fun MachineDefinitionBuilder<About.State>.aboutContentTransitions(
	machine: AboutMachine
) {
	from<About.State.Content> {
		// The consent toggle only renders with content on screen; persisting and the
		// state copy happen in the same row.
		on<About.Action.SetUsageDataCollectionEnabled> { state, action ->
			machine.persistUsageDataCollection(enabled = action.enabled)

			state.copy(usageDataCollectionEnabled = action.enabled)
		}
	}
}
