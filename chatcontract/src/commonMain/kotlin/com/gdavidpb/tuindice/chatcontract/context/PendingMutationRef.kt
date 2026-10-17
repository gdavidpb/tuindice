package com.gdavidpb.tuindice.chatcontract.context

import kotlinx.serialization.Serializable

/** A local mutation the client has not seen confirmed yet. */
@Serializable
public data class PendingMutationRef(
	public val mutationId: String,
	public val scope: String,
	public val status: String
)
