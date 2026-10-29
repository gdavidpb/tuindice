package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** Reference to a subject, with its status when it is known. */
@Serializable
public data class SubjectRef(
	public val code: String,
	public val name: String,
	public val status: String? = null
)
