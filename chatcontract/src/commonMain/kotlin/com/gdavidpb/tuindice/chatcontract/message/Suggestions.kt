package com.gdavidpb.tuindice.chatcontract.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Replies the user can tap instead of typing. */
@Serializable
@SerialName("suggestions")
public data class Suggestions(public val items: List<SuggestedReply>) : MessagePart
