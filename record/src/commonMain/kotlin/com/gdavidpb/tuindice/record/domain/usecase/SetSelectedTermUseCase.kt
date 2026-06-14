package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedTermParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetSelectedTermUseCase(
	private val recordSelectionRepository: RecordSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<SetSelectedTermParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: SetSelectedTermParams): Flow<Unit> {
		recordSelectionRepository.setSelectedTermId(
			viewMode = params.viewMode,
			termId = params.termId
		)
		return flowOf(Unit)
	}
}
