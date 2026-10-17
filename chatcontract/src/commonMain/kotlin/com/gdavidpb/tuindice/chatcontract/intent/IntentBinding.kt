package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/** Ties an intent to the mutations it runs, the record revision it was built on, and a hash of its args. */
@Serializable
public data class IntentBinding(
	public val mutationIds: List<String>,
	public val expectedRevision: Long? = null,
	public val argsHash: String
)
