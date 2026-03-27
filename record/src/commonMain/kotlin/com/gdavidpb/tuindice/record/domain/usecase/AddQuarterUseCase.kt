package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.toQuarterAdd
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.param.AddQuarterParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AddQuarterUseCase(
	private val quarterRepository: QuarterRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<AddQuarterParams, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: AddQuarterParams): Flow<Unit> {
		quarterRepository.addQuarter(add = params.toQuarterAdd())
		return flowOf(Unit)
	}
}
