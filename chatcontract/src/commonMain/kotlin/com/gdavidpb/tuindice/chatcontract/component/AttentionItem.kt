package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.Serializable

/** One item that needs attention; entityRef points to the record it concerns. */
@Serializable
public data class AttentionItem(
	public val kind: String,
	public val title: String,
	public val detail: String,
	public val entityRef: EntityRef? = null
)
