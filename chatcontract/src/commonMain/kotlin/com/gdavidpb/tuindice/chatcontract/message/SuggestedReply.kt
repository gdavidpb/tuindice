package com.gdavidpb.tuindice.chatcontract.message

import kotlinx.serialization.Serializable

/** Reply the user can tap instead of typing; [label] is its visible text. */
@Serializable
public data class SuggestedReply(public val id: String, public val label: String, public val text: String)
