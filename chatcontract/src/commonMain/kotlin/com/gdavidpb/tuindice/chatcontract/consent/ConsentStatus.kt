package com.gdavidpb.tuindice.chatcontract.consent

import kotlinx.serialization.Serializable

/** Whether the user accepted consent [version], and when, if they did. */
@Serializable
public data class ConsentStatus(
	public val version: Int,
	public val accepted: Boolean,
	public val acceptedAtMillis: Long? = null
)
