package com.gdavidpb.tuindice.chatcontract.feedback

import kotlinx.serialization.Serializable

/** Report of one assistant message, with an optional [comment] from the user. */
@Serializable
public data class MessageReport(
	public val messageId: String,
	public val reason: ReportReason,
	public val comment: String? = null
)
