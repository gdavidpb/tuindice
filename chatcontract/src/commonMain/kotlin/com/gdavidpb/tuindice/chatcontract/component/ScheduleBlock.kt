package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One class block; its minutes count from midnight. */
@Serializable
public data class ScheduleBlock(
	public val subjectCode: String,
	public val subjectName: String,
	public val startMinute: Int,
	public val endMinute: Int,
	public val room: String? = null,
	public val evaluationTitles: List<String> = emptyList()
)
