package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import kotlinx.coroutines.flow.Flow

interface ScheduleSelectionRepository {
	fun observeScheduleViewMode(): Flow<ScheduleViewMode>
	suspend fun setScheduleViewMode(viewMode: ScheduleViewMode)
}
