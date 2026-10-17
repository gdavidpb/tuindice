package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** Class blocks of one day; dayOfWeek goes from 1 (Monday) to 7 (Sunday). */
@Serializable
public data class ScheduleDay(
	public val dayOfWeek: Int,
	public val blocks: List<ScheduleBlock>
)
