package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation

interface MutationSyncSpec<ScopeKey, Command : OutboxMutation, Ack> {
	val maxRebaseAttempts: Int
		get() = 0

	/**
	 * How many executions may exhaust their rebases (and be parked as Failed, to be requeued later)
	 * before the row is parked for good. Unbounded unless a spec says otherwise.
	 */
	val maxExhaustedExecutions: Int
		get() = Int.MAX_VALUE

	fun deletePendingBeforeConfirm(
		mutation: MutationEnvelope<ScopeKey, Command>
	): Boolean = true

	/**
	 * The precondition [mutation] should be sent with right now. Called inside the scope's
	 * execution lock, so a revision that an earlier mutation just confirmed is already visible;
	 * the default keeps the one the row carries.
	 */
	suspend fun currentPrecondition(
		mutation: MutationEnvelope<ScopeKey, Command>
	): MutationPrecondition = mutation.precondition

	/** The revision the server answered with, when the acknowledgement carries one. */
	fun revisionOf(ack: Ack): Long? = null

	suspend fun send(
		mutation: MutationEnvelope<ScopeKey, Command>
	): Ack

	/**
	 * Applies the acknowledgement locally. The engine runs it under its write gate, which is also what
	 * the end of the session needs in order to change the session epoch. So it may only write locally:
	 * no request, and no suspension on anything that is not a local write. A confirmation that went to
	 * the network would hold the gate during the request, and a rejection met inside it (the 401 that
	 * ends the session) would wait for a gate that waits for the request. [send] is the place for
	 * requests: the gate is never held during it.
	 */
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
