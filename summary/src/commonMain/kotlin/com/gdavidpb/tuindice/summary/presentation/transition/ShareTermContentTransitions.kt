package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermMachine
import com.gdavidpb.tuindice.summary.presentation.machine.shareTermUnavailableState
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermContent

internal fun MachineDefinitionBuilder<ShareTerm.State>.shareTermContentTransitions(
	machine: ShareTermMachine,
	host: MachineHost<ShareTerm.Effect>
) {
	from<ShareTerm.State.Content> {
		// The terms are read again each minute: the sheet keeps the choice the student has made.
		on<ShareTermInternalEvent.TermsObserved> { state, event ->
			event.source.toShareTermContent(requestedTermKey = null, current = state)
		}

		onTo<ShareTermInternalEvent.TermsUnavailable, ShareTerm.State.Unavailable> { _, _ ->
			shareTermUnavailableState()
		}

		on<ShareTerm.Action.SelectTerm> { state, action ->
			if (state.terms.none { item -> item.key == action.termKey }) {
				state
			} else {
				state.withTermSelected(termKey = action.termKey)
			}
		}

		on<ShareTerm.Action.SelectBackground> { state, action ->
			if (state.backgrounds.none { item -> item.artwork.id == action.backgroundId }) {
				state
			} else {
				machine.rememberBackground(host = host, backgroundId = action.backgroundId)
				state.withBackgroundSelected(backgroundId = action.backgroundId)
			}
		}

		// One share at a time: the sheet is busy until the platform's share sheet has the image or has failed.
		on<ShareTerm.Action.ShareImage> { state, action ->
			if (state.isSharing) {
				state
			} else {
				machine.shareImage(host = host, picture = action.picture)
				state.copy(isSharing = true)
			}
		}

		on<ShareTermInternalEvent.ImageShared> { state, _ ->
			state.copy(isSharing = false)
		}

		on<ShareTermInternalEvent.ImageShareFailed>(
			emits = setOf(ShareTerm.Effect.ImageShareFailed::class)
		) { state, _ ->
			host.sendEffect(ShareTerm.Effect.ImageShareFailed)

			state.copy(isSharing = false)
		}
	}
}

private fun ShareTerm.State.Content.withTermSelected(termKey: String): ShareTerm.State.Content {
	return copy(terms = terms.map { item -> item.copy(selected = item.key == termKey) })
}

private fun ShareTerm.State.Content.withBackgroundSelected(backgroundId: String): ShareTerm.State.Content {
	return copy(backgrounds = backgrounds.map { item -> item.copy(selected = item.artwork.id == backgroundId) })
}
