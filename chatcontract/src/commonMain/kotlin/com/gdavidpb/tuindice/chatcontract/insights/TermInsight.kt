package com.gdavidpb.tuindice.chatcontract.insights

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.Serializable

/** Insight about one academic term, identified by [termKey]. */
@Serializable
public data class TermInsight(
	public val id: String,
	public val termKey: String,
	public val kind: String,
	public val title: String,
	public val body: String,
	public val entityRefs: List<EntityRef> = emptyList()
)
