package com.gdavidpb.tuindice.chatcontract.context

import kotlinx.serialization.Serializable

/** State of the client the service needs to plan a turn: the record revision it holds and its pending mutations. */
@Serializable
public data class ClientState(
	public val recordRevision: Long? = null,
	public val pendingMutations: List<PendingMutationRef> = emptyList()
)
