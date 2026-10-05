package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import kotlinx.coroutines.flow.Flow

interface ScheduleClockRepository {
	// The current moment, and again every time the minute changes.
	fun observeNow(): Flow<ScheduleNow>
}
