package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/**
 * A time clash: subjects that overlap on the same day and interval. [startBlock] to [endBlock] are the blocks the
 * subjects share, and [startMinute] to [endMinute] the hours of those blocks.
 */
@Serializable
public data class ScheduleClash(
	public val dayOfWeek: Int,
	public val startMinute: Int,
	public val endMinute: Int,
	public val startBlock: Int,
	public val endBlock: Int,
	public val subjectCodes: List<String>
)
