package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermMachine

internal fun MachineDefinitionBuilder<ShareTerm.State>.shareTermIdleTransitions(
	machine: ShareTermMachine,
	host: MachineHost<ShareTerm.Effect>
) {
	from<ShareTerm.State.Idle> {
		onTo<ShareTerm.Action.ObserveShareTerm, ShareTerm.State.Loading> { _, action ->
			machine.startObservation(host = host)
			ShareTerm.State.Loading(requestedTermKey = action.termKey)
		}
	}
}
