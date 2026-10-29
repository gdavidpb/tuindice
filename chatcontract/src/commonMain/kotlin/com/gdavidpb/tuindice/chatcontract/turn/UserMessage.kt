package com.gdavidpb.tuindice.chatcontract.turn

import kotlinx.serialization.Serializable

/** What the user sent in a turn: free text, a suggested reply, or attachments. */
@Serializable
public data class UserMessage(
	public val text: String? = null,
	public val suggestionId: String? = null,
	public val attachmentIds: List<String> = emptyList()
)
