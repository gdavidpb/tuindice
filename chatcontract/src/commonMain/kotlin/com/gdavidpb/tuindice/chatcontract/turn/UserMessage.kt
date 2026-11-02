package com.gdavidpb.tuindice.chatcontract.turn

import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import kotlinx.serialization.Serializable

/**
 * What the user sent in a turn: free text, a suggested reply, or attachments with or without text. [attachmentIds] are
 * the ids their uploads returned, at most [AttachmentLimits.MAX_PER_MESSAGE].
 */
@Serializable
public data class UserMessage(
	public val text: String? = null,
	public val suggestionId: String? = null,
	public val attachmentIds: List<String> = emptyList()
)
