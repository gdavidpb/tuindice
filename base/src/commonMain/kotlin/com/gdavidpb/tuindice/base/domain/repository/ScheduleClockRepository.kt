package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import kotlinx.coroutines.flow.Flow

interface ScheduleClockRepository {
	// The current moment, and again every time the minute changes.
	//
	// The ticker sleeps with the process, so what was last delivered can be up to a minute old when
	// the app comes back. A consumer that draws from it must therefore read it again when it is
	// resumed (see ResumedEffect and the schedule sheet, which restarts its observation then); the
	// first resumption is the opening itself, which already read it.
	fun observeNow(): Flow<ScheduleNow>
}
