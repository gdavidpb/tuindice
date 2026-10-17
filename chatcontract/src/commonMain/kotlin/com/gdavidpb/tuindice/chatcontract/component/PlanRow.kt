package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** Plan of one term: its key, its label, its subjects and its credits. */
@Serializable
public data class PlanRow(
	public val termKey: String,
	public val termLabel: String,
	public val subjectCodes: List<String>,
	public val credits: Int
)
