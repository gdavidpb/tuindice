package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.toQuarterAdd
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.param.AddQuarterParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AddQuarterUseCase(
	private val quarterRepository: QuarterRepository
) : FlowUseCase<AddQuarterParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: AddQuarterParams): Flow<Unit> {
		quarterRepository.addQuarter(add = params.toQuarterAdd())
		return flowOf(Unit)
	}
}
