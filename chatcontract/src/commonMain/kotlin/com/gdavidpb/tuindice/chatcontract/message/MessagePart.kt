package com.gdavidpb.tuindice.chatcontract.message

import kotlinx.serialization.Serializable

/** Piece of an [AssistantMessage]. A part type this build does not know decodes to [UnknownMessagePart]. */
@Serializable
public sealed interface MessagePart
