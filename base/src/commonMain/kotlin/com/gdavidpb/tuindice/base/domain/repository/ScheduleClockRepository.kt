package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import kotlinx.coroutines.flow.Flow

interface ScheduleClockRepository {
	// The current moment, and again every time the minute changes.
	fun observeNow(): Flow<ScheduleNow>
}
