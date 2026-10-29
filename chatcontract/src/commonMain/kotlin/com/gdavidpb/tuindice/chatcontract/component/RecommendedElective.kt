package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One recommended elective, and the reason it is recommended. */
@Serializable
public data class RecommendedElective(
	public val code: String,
	public val name: String,
	public val credits: Int,
	public val reason: String
)
