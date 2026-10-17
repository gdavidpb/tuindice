package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One candidate plan of a comparison. */
@Serializable
public data class PlanOption(
	public val label: String,
	public val credits: Int,
	public val loadBand: String,
	public val unlocks: List<String> = emptyList(),
	public val clashes: Int
)
