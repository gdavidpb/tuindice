package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetRecordViewModeUseCase(
	private val recordSelectionRepository: RecordSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<RecordViewMode, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: RecordViewMode): Flow<Unit> {
		recordSelectionRepository.setRecordViewMode(params)
		return flowOf(Unit)
	}
}
