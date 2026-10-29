package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One subject of a list, with its status when it is known. */
@Serializable
public data class SubjectRow(
	public val code: String,
	public val name: String,
	public val credits: Int,
	public val status: String? = null
)
