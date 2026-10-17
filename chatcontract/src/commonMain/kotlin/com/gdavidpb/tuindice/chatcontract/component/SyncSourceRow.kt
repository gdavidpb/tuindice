package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One source of the sync state and its status. */
@Serializable
public data class SyncSourceRow(
	public val name: String,
	public val status: String,
	public val detail: String? = null
)
