package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/** The app's report of how it ended the intent [intentId], with the mutations it ran. */
@Serializable
public data class IntentOutcomeReport(
	public val intentId: String,
	public val outcome: OutcomeKind,
	public val mutationIds: List<String> = emptyList(),
	public val detailCode: String? = null,
	public val newRevision: Long? = null
)
