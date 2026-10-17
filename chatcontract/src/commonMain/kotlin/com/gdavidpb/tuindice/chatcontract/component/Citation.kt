package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One cited source: its document, its article, and an excerpt. */
@Serializable
public data class Citation(
	public val document: String,
	public val article: String,
	public val excerpt: String,
	public val url: String? = null
)
