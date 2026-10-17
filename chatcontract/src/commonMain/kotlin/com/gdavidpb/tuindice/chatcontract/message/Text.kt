package com.gdavidpb.tuindice.chatcontract.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Plain text of the message. */
@Serializable
@SerialName("text")
public data class Text(public val text: String) : MessagePart
