package com.gdavidpb.tuindice.chatcontract.feedback

import kotlinx.serialization.Serializable

/** The user's thumbs up or down on one assistant message. */
@Serializable
public data class MessageFeedback(public val messageId: String, public val useful: Boolean)
