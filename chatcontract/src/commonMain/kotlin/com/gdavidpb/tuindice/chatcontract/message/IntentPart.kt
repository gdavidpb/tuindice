package com.gdavidpb.tuindice.chatcontract.message

import com.gdavidpb.tuindice.chatcontract.intent.Intent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An intent the user can confirm or cancel from the message. */
@Serializable
@SerialName("intent")
public data class IntentPart(public val intent: Intent) : MessagePart
