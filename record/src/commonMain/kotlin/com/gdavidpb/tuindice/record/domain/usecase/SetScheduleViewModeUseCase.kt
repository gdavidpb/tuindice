package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.domain.repository.ScheduleSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetScheduleViewModeUseCase(
	private val scheduleSelectionRepository: ScheduleSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<ScheduleViewMode, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: ScheduleViewMode): Flow<Unit> {
		scheduleSelectionRepository.setScheduleViewMode(params)
		return flowOf(Unit)
	}
}
