package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One term of an index series. */
@Serializable
public data class IndexPoint(
	public val termId: String,
	public val termLabel: String,
	public val termIndex: Double?,
	public val cumulativeIndex: Double?
)
