package com.gdavidpb.tuindice.pensum.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumInternalEvent
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumMachine

internal fun MachineDefinitionBuilder<Pensum.State>.idleTransitions(
	machine: PensumMachine,
	host: MachineHost<Pensum.Effect>
) {
	from<Pensum.State.Idle> {
		on<Pensum.Action.ObservePensum> { state, _ ->
			machine.startObservation(host = host)
			state
		}

		on<Pensum.Action.EnsurePensumLoaded> { state, _ ->
			machine.ensurePensumLoaded(host = host)
			state
		}

		on<PensumInternalEvent.PensumRefreshLoading> { state, _ -> state }
	}
}
