package com.gdavidpb.tuindice.chatcontract.memory

import kotlinx.serialization.Serializable

/** Everything the assistant remembers about the user. */
@Serializable
public data class MemoryList(public val items: List<MemoryItem>)
