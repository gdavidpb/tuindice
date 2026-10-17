package com.gdavidpb.tuindice.chatcontract.error

import kotlinx.serialization.Serializable

/** Body of an error answer of the assistant endpoints; [code] is one of the [AssistantErrorCodes]. */
@Serializable
public data class AssistantErrorBody(
	public val code: String,
	public val message: String? = null,
	public val retryAfterSeconds: Int? = null
)
