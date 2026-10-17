package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** A time clash: subjects that overlap on the same day and interval. */
@Serializable
public data class ScheduleClash(
	public val dayOfWeek: Int,
	public val startMinute: Int,
	public val endMinute: Int,
	public val subjectCodes: List<String>
)
