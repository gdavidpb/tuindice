package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Asks to remember [statement] about the user, with the [quote] it comes from. The server applies it.
 * The anchor is the fact the memory hangs from: [anchorKind] is the name of academiccore's
 * `MemoryAnchorKind`, [anchorRef] identifies it and [anchorDate] (ISO `yyyy-MM-dd`) bounds it.
 * A memory without an anchor leaves the three out. [replacesMemoryId] is the id of the memory this one
 * replaces, when the student corrects one that already exists; null adds it beside the others.
 */
@Serializable
@SerialName("memoria.recordar/1")
public data class RememberArgs(
	public val statement: String,
	public val category: String,
	public val quote: String,
	public val anchorKind: String? = null,
	public val anchorRef: String? = null,
	public val anchorDate: String? = null,
	public val replacesMemoryId: String? = null
) : IntentArgs
