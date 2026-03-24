package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation

interface MutationProjectionSpec<ScopeKey, Command : OutboxMutation, ConfirmedState, VisibleState, Resolution> {
	fun projectVisibleState(
		confirmedState: ConfirmedState,
		pendingMutations: List<MutationEnvelope<ScopeKey, Command>>
	): VisibleState

	fun resolveIncomingState(
		incomingConfirmedState: ConfirmedState,
		pendingMutations: List<MutationEnvelope<ScopeKey, Command>>
	): Resolution
}
