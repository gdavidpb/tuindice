package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One way to reach support: a [label] with a [phone] number, a [url], or both. */
@Serializable
public data class SupportLine(
	public val label: String,
	public val phone: String? = null,
	public val url: String? = null
)
