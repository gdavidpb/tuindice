package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/**
 * How one mutation of an intent ended: the mutation [mutationId], its [outcome], the detail it carries, and the revision it
 * left, when known. A batch reports one per mutation it ran, so a partial batch can be told from a whole one.
 */
@Serializable
public data class MutationOutcome(
	public val mutationId: String,
	public val outcome: OutcomeKind,
	public val detailCode: String? = null,
	public val newRevision: Long? = null
)
