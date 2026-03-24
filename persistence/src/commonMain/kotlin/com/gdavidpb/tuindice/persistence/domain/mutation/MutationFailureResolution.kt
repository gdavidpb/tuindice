package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation

sealed class MutationFailureResolution<ScopeKey, Command : OutboxMutation> {
	data class Retry<ScopeKey, Command : OutboxMutation>(
		val mutation: MutationEnvelope<ScopeKey, Command>
	) : MutationFailureResolution<ScopeKey, Command>()

	data class Drop<ScopeKey, Command : OutboxMutation>(
		val propagate: Boolean = false
	) : MutationFailureResolution<ScopeKey, Command>()

	data class Fail<ScopeKey, Command : OutboxMutation>(
		val propagate: Boolean = true
	) : MutationFailureResolution<ScopeKey, Command>()
}
