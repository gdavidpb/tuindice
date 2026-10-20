package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/**
 * One class block: its [startBlock] to [endBlock], where block 1 starts at 7:30 and each block lasts an hour.
 * Its minutes count from midnight.
 */
@Serializable
public data class ScheduleBlock(
	public val subjectCode: String,
	public val subjectName: String,
	public val startMinute: Int,
	public val endMinute: Int,
	public val startBlock: Int,
	public val endBlock: Int,
	public val room: String? = null,
	public val evaluationTitles: List<String> = emptyList()
)
