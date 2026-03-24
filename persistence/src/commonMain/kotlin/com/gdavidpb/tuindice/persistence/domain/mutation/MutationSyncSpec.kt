package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation

interface MutationSyncSpec<ScopeKey, Command : OutboxMutation, ConfirmedState, VisibleState, Ack> {
	val maxRebaseAttempts: Int
		get() = 0

	fun deletePendingBeforeConfirm(
		mutation: MutationEnvelope<ScopeKey, Command>
	): Boolean = true

	suspend fun send(
		mutation: MutationEnvelope<ScopeKey, Command>
	): Ack

	suspend fun confirm(
		mutation: MutationEnvelope<ScopeKey, Command>,
		ack: Ack
	)

	fun classifyError(
		mutation: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable
	): MutationFailureKind = MutationFailureKind.Terminal

	suspend fun rebase(
		mutation: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable
	): MutationEnvelope<ScopeKey, Command>? = null

	suspend fun resolveFailure(
		mutation: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable
	): MutationFailureResolution<ScopeKey, Command> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> {
				val rebasedMutation = rebase(mutation, throwable)
				if (rebasedMutation == null) {
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Retry(rebasedMutation)
				}
			}

			MutationFailureKind.NotFound ->
				MutationFailureResolution.Drop(propagate = true)

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}
}
