package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Asks to remember [statement] about the user, with the [quote] it comes from. The server applies it. */
@Serializable
@SerialName("memoria.recordar/1")
public data class RememberArgs(
	public val statement: String,
	public val category: String,
	public val quote: String
) : IntentArgs
