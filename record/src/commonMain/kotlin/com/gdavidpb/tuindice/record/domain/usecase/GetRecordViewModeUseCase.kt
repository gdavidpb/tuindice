package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetRecordViewModeUseCase(
	private val quarterSelectionRepository: QuarterSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, RecordViewMode, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<RecordViewMode> {
		return flowOf(quarterSelectionRepository.getRecordViewMode())
	}
}
