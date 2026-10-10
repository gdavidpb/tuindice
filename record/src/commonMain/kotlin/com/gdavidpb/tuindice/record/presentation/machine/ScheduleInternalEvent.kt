package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem

/**
 * Internal machine inputs for the schedule screen. The observation resolution
 * (content/empty/waiting) is split per outcome so every row keeps a fixed target.
 */
sealed interface ScheduleInternalEvent {
	data class ScheduleContentObserved(
		val termName: String,
		val schedule: ScheduleItem,
		val viewMode: ScheduleViewMode
	) : ScheduleInternalEvent

	data object ScheduleEmptyObserved : ScheduleInternalEvent

	data object ScheduleWaitingObserved : ScheduleInternalEvent
}
