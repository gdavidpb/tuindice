package com.gdavidpb.tuindice.chatcontract.memory

import kotlinx.serialization.Serializable

/** Something the assistant remembers about the user. [anchorLabel] names what the memory is about, when known. */
@Serializable
public data class MemoryItem(
	public val id: String,
	public val category: String,
	public val text: String,
	public val state: String,
	public val anchorLabel: String? = null,
	public val createdAtMillis: Long
)
