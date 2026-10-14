package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermInternalEvent

// The sheet does not retry: a failed observation ends the flow, so nothing brings the terms back. Opening the
// sheet again reads them again.
internal fun MachineDefinitionBuilder<ShareTerm.State>.shareTermUnavailableTransitions() {
	from<ShareTerm.State.Unavailable> {
		on<ShareTermInternalEvent.TermsUnavailable> { state, _ -> state }
	}
}
