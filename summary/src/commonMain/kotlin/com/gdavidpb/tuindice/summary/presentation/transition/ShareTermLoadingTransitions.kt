package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.shareTermUnavailableState
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermContent

internal fun MachineDefinitionBuilder<ShareTerm.State>.shareTermLoadingTransitions() {
	from<ShareTerm.State.Loading> {
		// The term the sheet was opened on is selected, when the terms have it.
		onTo<ShareTermInternalEvent.TermsObserved, ShareTerm.State.Content> { state, event ->
			event.source.toShareTermContent(requestedTermKey = state.requestedTermKey, current = null)
		}

		onTo<ShareTermInternalEvent.TermsUnavailable, ShareTerm.State.Unavailable> { _, _ ->
			shareTermUnavailableState()
		}
	}
}
