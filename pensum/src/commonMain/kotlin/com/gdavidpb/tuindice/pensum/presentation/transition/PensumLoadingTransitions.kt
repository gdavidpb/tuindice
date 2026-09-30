package com.gdavidpb.tuindice.pensum.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum

internal fun MachineDefinitionBuilder<Pensum.State>.loadingTransitions() {
	from<Pensum.State.Loading> {
		// A load is already in flight; re-entering the tab waits for it.
		on<Pensum.Action.EnsurePensumLoaded> { state, _ -> state }
	}
}
